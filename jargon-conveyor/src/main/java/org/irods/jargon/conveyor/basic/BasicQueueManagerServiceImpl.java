/**
 * 
 */
package org.irods.jargon.conveyor.basic;

import java.util.Date;
import java.util.List;

import org.irods.jargon.conveyor.core.AbstractConveyorComponentService;
import org.irods.jargon.conveyor.core.ConveyorBusyException;
import org.irods.jargon.conveyor.core.ConveyorExecutionException;
import org.irods.jargon.conveyor.core.ConveyorExecutorService.ErrorStatus;
import org.irods.jargon.conveyor.core.ConveyorService;
import org.irods.jargon.conveyor.core.GridAccountService;
import org.irods.jargon.conveyor.core.QueueManagerService;
import org.irods.jargon.conveyor.core.RejectedTransferException;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.TransferAttemptDAO;
import org.irods.jargon.transfer.dao.TransferDAO;
import org.irods.jargon.transfer.dao.TransferDAOException;
import org.irods.jargon.transfer.dao.domain.GridAccount;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferAttempt;
import org.irods.jargon.transfer.dao.domain.TransferState;
import org.irods.jargon.transfer.dao.domain.TransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic implementation of a queue manager service
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
@Transactional(rollbackFor = { ConveyorExecutionException.class })
public class BasicQueueManagerServiceImpl extends
		AbstractConveyorComponentService implements QueueManagerService {

	/**
	 * Injected dependency
	 */
	private TransferDAO transferDAO;

	/**
	 * Injected dependency
	 */
	private TransferAttemptDAO transferAttemptDAO;

	/**
	 * Injected dependency
	 */
	private ConveyorService conveyorService;

	/**
	 * Injected dependency
	 */
	private GridAccountService gridAccountService;

	private static final Logger log = LoggerFactory
			.getLogger(BasicQueueManagerServiceImpl.class);

	public void enqueueRestartOfTransferOperation(final long transferId)
			throws RejectedTransferException, ConveyorExecutionException {

		log.info("enqueueTransferOperation()");

		if (transferId <= 0) {
			throw new IllegalArgumentException("illegal transferId");
		}

		log.info("transferId:{}", transferId);
		log.info("looking up transfer to restart...");

		Transfer transfer;
		try {
			transfer = transferDAO.findById(new Long(transferId));
		} catch (TransferDAOException e) {
			log.error("error looking up transfer by id", e);
			throw new ConveyorExecutionException(
					"unable to lookup transfer by id", e);
		}

		/*
		 * Check state of transfer and throw a rejected exception if this
		 * transfer was rejected for some reason
		 */
		evaluateTransferForExecution(transfer);

		TransferAttempt lastTransferAttempt;
		try {
			lastTransferAttempt = transferAttemptDAO
					.findLastTransferAttemptForTransferByTransferId(transferId);
		} catch (TransferDAOException e) {
			log.error("error looking up last transfer attempt", e);
			throw new ConveyorExecutionException(
					"unable to lookup last transfer attempt", e);
		}

		if (lastTransferAttempt == null) {
			throw new RejectedTransferException(
					"no previous attempt found to base restart on");
		}

		log.info("building transfer attempt based on previous attempt...");
		transfer.setTransferState(TransferState.ENQUEUED);
		transfer.setUpdatedAt(new Date());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#enqueueTransferOperation
	 * (org.irods.jargon.transfer.dao.domain.Transfer,
	 * org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public void enqueueTransferOperation(final Transfer transfer,
			final IRODSAccount irodsAccount) throws RejectedTransferException,
			ConveyorExecutionException {

		log.info("enqueueTransferOperation()");

		if (transfer == null) {
			throw new IllegalArgumentException("null transfer");
		}

		if (irodsAccount == null) {
			throw new IllegalArgumentException("null irodsAccount");
		}

		log.info("transfer:{}", transfer);
		log.info("irodsAccount:{}", irodsAccount);

		log.info("looking up corresponding GridAccount...");
		GridAccount gridAccount = gridAccountService
				.findGridAccountByIRODSAccount(irodsAccount);
		if (gridAccount == null) {
			log.error("error finding grid account for irodsAccount:{}",
					irodsAccount);
			throw new ConveyorExecutionException(
					"unable to resolve gridAccount from given irodsAccount");
		}

		log.info("building transfer...");

		/*
		 * Check to see if there is any reason to reject this transfer
		 * (malformed, duplicate). The evaluate method will throw a rejected
		 * exception if appropriate
		 */
		evaluateTransferForExecution(transfer);

		transfer.setGridAccount(gridAccount);
		transfer.setTransferState(TransferState.ENQUEUED);
		transfer.setUpdatedAt(new Date());

		/*
		 * Enqueue triggers a dequeue
		 */

		try {
			transferDAO.save(transfer);
			conveyorService.getTransferAccountingManagementService()
					.prepareTransferForProcessing(transfer);
			dequeueNextOperation();
		} catch (TransferDAOException e) {
			log.error("error saving transfer", e);
			throw new ConveyorExecutionException("error saving transfer", e);
		}

		log.info("transfer added:{}", transfer);

	}

	private void evaluateTransferForExecution(Transfer transfer)
			throws RejectedTransferException, ConveyorExecutionException {
		// FIXME: implement this!

	}

	@Override
	public List<Transfer> listAllTransfersInQueue()
			throws ConveyorExecutionException {
		log.info("listAllTransfersInQueue");

		try {
			return transferDAO.findAll();
		} catch (TransferDAOException e) {
			log.error("error listing all transfers", e);
			throw new ConveyorExecutionException("error listing transfers", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#dequeueNextOperation()
	 */
	@Override
	public void dequeueNextOperation() throws ConveyorExecutionException {
		log.info("dequeueNextOperation()");

		try {
			this.getConveyorExecutorService().setBusyForAnOperation();
		} catch (ConveyorBusyException e) {
			log.info("busy, ignore..");
			return;
		}

		try {

			List<Transfer> transfers = transferDAO
					.findByTransferState(TransferState.ENQUEUED);
			// Transfer transfer = new Transfer(); // fake code for above

			if (transfers.isEmpty()) {
				log.info("nothing to process...");
				this.getConveyorExecutorService().setOperationCompleted();
				return;
			}

			Transfer transfer = transfers.get(0);
			log.info("have transfer to run... setting up the new attempt:{}",
					transfer);

			// upon dequeue clear the error status
			this.getConveyorExecutorService().setErrorStatus(ErrorStatus.OK);
			TransferAttempt transferAttempt = transferAttemptDAO
					.findLastTransferAttemptForTransferByTransferId(transfer
							.getId());

			if (transferAttempt == null) {
				log.warn(
						"transfer attempt is not available in the transfer:{}",
						transfer);
			}

			transferAttempt.setAttemptStart(new Date());
			transfer.setTransferState(TransferState.PROCESSING);
			transfer.setUpdatedAt(new Date());
			transferDAO.save(transfer);

			this.getConveyorExecutorService().processTransfer(transferAttempt,
					this.conveyorService);

		} catch (JargonException je) {
			log.error("jargon exception dequeue operation, will unlock queue");
			this.getConveyorExecutorService().setOperationCompleted();
			this.getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(je);
			this.dequeueNextOperation();
		} catch (Exception e) {
			log.error("jargon exception dequeue operation, will unlock queue");
			this.getConveyorExecutorService().setOperationCompleted();
			this.getConveyorService().getConveyorCallbackListener()
					.signalUnhandledConveyorException(e);
			this.dequeueNextOperation();

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#processTransfer(java
	 * .lang.String, java.lang.String,
	 * org.irods.jargon.core.connection.IRODSAccount,
	 * org.irods.jargon.transfer.dao.domain.TransferType)
	 */
	@Override
	public void enqueueTransferOperation(String irodsFile, String localFile,
			IRODSAccount irodsAccount, TransferType type)
			throws ConveyorExecutionException {
		log.info("processTransfer()");
		Transfer transfer = new Transfer();
		transfer.setCreatedAt(new Date());
		transfer.setIrodsAbsolutePath(irodsFile);
		transfer.setLocalAbsolutePath(localFile);
		transfer.setTransferType(type);
		log.info("ready to enqueue transfer:{}", transfer);
		enqueueTransferOperation(transfer, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#purgeAllFromQueue()
	 */
	@Override
	public void purgeAllFromQueue() throws ConveyorBusyException,
			ConveyorExecutionException {
		log.info("purgeAllFromQueue()");

		log.info("see if conveyor is busy");

		try {
			this.getConveyorExecutorService().setBusyForAnOperation();
		} catch (ConveyorBusyException e) {
			log.info("conveyor is busy, cannot purge");
			throw e;
		}

		log.info("purge...");

		try {
			transferDAO.purgeEntireQueue();
		} catch (TransferDAOException e) {
			log.error("jargon exception dequeue operation, will unlock queue");
			throw new ConveyorExecutionException(e);
		} finally {
			this.getConveyorExecutorService().setOperationCompleted();

		}

	}

	@Override
	public Transfer initializeGivenTransferByLoadingChildren(
			final Transfer transfer) throws ConveyorExecutionException {
		log.info("initializeGivenTransferByLoadingChildren");
		try {
			return transferDAO.initializeChildrenForTransfer(transfer);
		} catch (TransferDAOException e) {
			log.error("jargon exception dequeue operation, will unlock queue");
			throw new ConveyorExecutionException(e);
		}
	}

	/**
	 * @return the transferDAO
	 */
	public TransferDAO getTransferDAO() {
		return transferDAO;
	}

	/**
	 * @param transferDAO
	 *            the transferDAO to set
	 */
	public void setTransferDAO(final TransferDAO transferDAO) {
		this.transferDAO = transferDAO;
	}

	/**
	 * @return the transferAttemptDAO
	 */
	public TransferAttemptDAO getTransferAttemptDAO() {
		return transferAttemptDAO;
	}

	/**
	 * @param transferAttemptDAO
	 *            the transferAttemptDAO to set
	 */
	public void setTransferAttemptDAO(
			final TransferAttemptDAO transferAttemptDAO) {
		this.transferAttemptDAO = transferAttemptDAO;
	}

	/**
	 * @return the gridAccountService
	 */
	public GridAccountService getGridAccountService() {
		return gridAccountService;
	}

	/**
	 * @param gridAccountService
	 *            the gridAccountService to set
	 */
	public void setGridAccountService(
			final GridAccountService gridAccountService) {
		this.gridAccountService = gridAccountService;
	}

	/**
	 * @return the conveyorService
	 */
	public ConveyorService getConveyorService() {
		return conveyorService;
	}

	/**
	 * @param conveyorService
	 *            the conveyorService to set
	 */
	public void setConveyorService(ConveyorService conveyorService) {
		this.conveyorService = conveyorService;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.conveyor.core.QueueManagerService#findTransferByTransferId
	 * (long)
	 */
	@Override
	public Transfer findTransferByTransferId(long transferId)
			throws ConveyorExecutionException {
		log.info("initializeGivenTransferByLoadingChildren");
		try {
			return transferDAO.findInitializedById(transferId);
		} catch (TransferDAOException e) {
			log.error("error in dao finding transfer by id");
			throw new ConveyorExecutionException(e);
		}
	}

}
