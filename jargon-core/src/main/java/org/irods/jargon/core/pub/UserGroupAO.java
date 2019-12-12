package org.irods.jargon.core.pub;

import java.util.List;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.utils.MiscIRODSUtils;

/**
 * Interface for an access object dealing with iRODS user groups. Includes
 * methods to obtain information on, and to manage iRODS user groups.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public interface UserGroupAO extends IRODSAccessObject {

	/**
	 * Given a user group id, return the {@code UserGroup}, or return null if not
	 * found
	 *
	 * @param userGroupId {@code String} with the numeric key for the user group
	 * @return {@code UserGroup} domain object
	 * @throws JargonException {@link JargonException}
	 */
	UserGroup find(final String userGroupId) throws JargonException;

	/**
	 * Given a user name, return the {@code UserGroup}, or return null if not found
	 *
	 * @param userGroupName {@code String} with the name of the user group
	 * @return {@code UserGroup} domain object
	 * @throws JargonException {@link JargonException}
	 */
	UserGroup findByName(final String userGroupName) throws JargonException;

	/**
	 * Given a user name, return the user groups that the given user belongs to, or
	 * an empty {@code List} when no user groups are found.
	 *
	 * @param userName {@code String} with an IRODS user name
	 * @return {@code List} of {@link UserGroup}
	 * @throws JargonException {@link JargonException}
	 */
	List<UserGroup> findUserGroupsForUser(String userName) throws JargonException;

	/**
	 * Add the given user group to iRODS. NB a group can only be in the same zone as
	 * the logged in server.
	 *
	 * @param userGroup {@link UserGroup} to add
	 * @throws DuplicateDataException if user already exists
	 * @throws JargonException        {@link JargonException}
	 */
	void addUserGroup(UserGroup userGroup) throws DuplicateDataException, JargonException;

	/**
	 * Remove the given user group from iRODS. Note that if the user group is not
	 * found, a warning is logged, and the exception is ignored. NB you cannot
	 * remove a group from a zone other than the logged-in zone.
	 *
	 * @param userGroup {@link UserGroup} to remove
	 * @throws JargonException {@link JargonException}
	 */
	void removeUserGroup(UserGroup userGroup) throws JargonException;

	/**
	 * List the {@code User}s that are members of an iRODS {@code UserGroup}.
	 *
	 * @param userGroupName {@code String} with the name of an iRODS user group
	 * @return {@code List} of {@link User} with the group membership. This will be
	 *         an empty {@code List} if the group has no members.
	 * @throws JargonException {@link JargonException}
	 */
	List<User> listUserGroupMembers(String userGroupName) throws JargonException; // FIXME: add list by name and zone

	/**
	 * Add the given user to the iRODS user group
	 *
	 * @param userGroupName {@code String} with the name of the iRODS user group.
	 *                      This group must exist.
	 * @param userName      {@code String} with the name of the iRODS user to add to
	 *                      the group. This user must exist.
	 * @param zoneName      {@code String} with the name of the iRODS zone for the
	 *                      user. This is optional and may be set to blank or
	 *                      {@code null} if not needed.
	 * @throws DuplicateDataException if the user is already a group member
	 * @throws InvalidGroupException  for invalid group
	 * @throws InvalidUserException   for invalid user
	 * @throws JargonException        {@link JargonException}
	 */
	void addUserToGroup(String userGroupName, String userName, String zoneName)
			throws InvalidGroupException, InvalidUserException, JargonException;

	/**
	 * Remove the given user (with optional zone) from the given group. If the user
	 * is valid but not in group, the method will return normally.
	 *
	 * @param userGroupName {@code String} with the name of the iRODS user group.
	 * @param userName      {@code String} with the name of the iRODS user to add to
	 *                      the group.
	 * @param zoneName      {@code String} with the name of the iRODS zone for the
	 *                      user. This is optional and may be set to blank or
	 *                      {@code null} if not needed.
	 * @throws InvalidUserException  for invalid user
	 * @throws InvalidGroupException for invalid group
	 * @throws JargonException       {@link JargonException}
	 */
	void removeUserFromGroup(String userGroupName, String userName, String zoneName)
			throws InvalidUserException, InvalidGroupException, JargonException;

	/**
	 * List all user groups
	 *
	 * @return {@code List} of {@link UserGroup}
	 * @throws JargonException {@link JargonException}
	 */
	List<UserGroup> findAll() throws JargonException;

	/**
	 * Query the ICAT and see if the given user is in the given group
	 *
	 * @param userName  {@code String} with the user name
	 * @param groupName {@code String} with the group name
	 * @return {@code boolean} which will be {@code true} if the user is in the
	 *         given group
	 * @throws JargonException {@link JargonException}
	 */
	boolean isUserInGroup(String userName, String groupName) throws JargonException;

	/**
	 * Handy method to remove a user group in the current zone by simply giving the
	 * user group name. This method will treat a non-existent group as if it had
	 * been deleted, logging this situation and proceeding.
	 *
	 * @param userGroupName {@code String} with the name of the user group to
	 *                      delete.
	 * @throws JargonException for iRODS error
	 */
	void removeUserGroup(String userGroupName) throws JargonException;

	/**
	 * Given a search term, find user groups like that term. A blank term will find
	 * all. This default version does a case-sensitive search.
	 *
	 * @param userGroupName {@code String} with search term
	 * @throws JargonException {@link JargonException}
	 * @return {@code List} of {@link UserGroup}
	 * 
	 */
	List<UserGroup> findUserGroups(String userGroupName) throws JargonException;

	/**
	 * Given a search term, find user groups like that term. A blank term will find
	 * all. This default version does a case-sensitive search.
	 *
	 * @param userGroupName {@code String} with search term
	 * @throws JargonException {@link JargonException}
	 * @return {@code List} of {@link UserGroup}
	 * 
	 */
	List<String> findUserGroupNames(String userGroupName, boolean caseInsensitive) throws JargonException;

	/**
	 * Given a search term, find user groups like that term. A blank term will find
	 * all. This variant can do a case-insensitive search
	 *
	 * @param userGroupName   {@code String} with search term
	 * @param caseInsensitive {@code boolean} that will do a case-insensitive search
	 *                        if true
	 * @throws JargonException {@link JargonException}
	 * @return {@code List} of {@link UserGroup}
	 * 
	 */
	List<UserGroup> findUserGroups(String userGroupName, boolean caseInsensitive) throws JargonException;

	/**
	 * Add a user group as a user with group admin privileges
	 *
	 * @param userGroup {@link UserGroup} to add
	 * @throws DuplicateDataException {@link DuplicateDataException}
	 * @throws JargonException        {@link JargonException}
	 */
	void addUserGroupAsGroupAdmin(final UserGroup userGroup) throws DuplicateDataException, JargonException;

	/**
	 * Add the given user to the group as a user with group admin privileges
	 *
	 * @param userGroupName {@code String} of the group to which the user will be
	 *                      added
	 * @param userName      {@code String} with the user name
	 * @param zoneName      {@code String} with the zone to which the user will be
	 *                      added
	 * @throws DuplicateDataException {@link DuplicateDataException}
	 * @throws InvalidGroupException  {@link InvalidGroupException}
	 * @throws InvalidUserException   {@link InvalidUserException}
	 * @throws JargonException        {@link JargonException}
	 */
	void addUserToGroupAsGroupAdmin(String userGroupName, String userName, String zoneName)
			throws DuplicateDataException, InvalidGroupException, InvalidUserException, JargonException;

	/**
	 * Find the user groups to which the given user is a member. This can be used to
	 * find user membership in a different zone
	 * 
	 * @param userName   {@code String} with the user name. The user name must be
	 *                   supplied in 'user#zone' format if the user is a federated
	 *                   user.
	 * @param targetZone {@code String} with the zone of interest to be queried.
	 *                   Note if the target zone is set to blank (it cannot be null)
	 *                   then it will operate as the within zone form
	 * @return {@code List} of {@link UserGroup}
	 * @throws JargonException {@link JargonException}
	 */
	List<UserGroup> findUserGroupsForUserInZone(final String userName, final String targetZone) throws JargonException;

	/**
	 * List the members of the given user group. This query can look for user groups
	 * in a federated zone through the {@code targetZone} parameter
	 * 
	 * @param userGroupName {@code String} with the user group name to be searched
	 *                      to obtain membership. The user name must be supplied in
	 *                      'user#zone' format if the user is a federated user.
	 * @param targetZone    {@code String} with the zone to query to list group
	 *                      members. Note that if the zone is blank (it cannot be
	 *                      null) the method will proceed with the no-zone method
	 *                      variant
	 * @return {@code List} of {@link User} for the users in the given zone
	 * @throws JargonException {@link JargonException}
	 */
	List<User> listUserGroupMembers(final String userGroupName, final String targetZone) throws JargonException;

	/**
	 * Handy util method to split up a user group name into group name and optional
	 * zone
	 * 
	 * @param userGroupName {@code String} with the name of the user group
	 * @return {@link UserGroup} containing the broke-out name
	 */
	public static UserGroup splitGroupIntoNameAndZone(final String userGroupName) {
		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("userGroupName is null or empty");
		}

		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupName(MiscIRODSUtils.getUserInUserName(userGroupName));
		userGroup.setZone(MiscIRODSUtils.getZoneInUserName(userGroupName));
		return userGroup;

	}

}