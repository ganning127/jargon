
Jargon Core API

work on milestone: https://github.com/DICE-UNC/jargon/issues?milestone=4&state=open

# Project: Jargon-core API
#### Date:
#### Release Version: 4.0.2.1-SNAPSHOT
#### git tag: MASTER
#### Developer: Mike Conway - DICE
## News

This release marks compatability with iRODS 4.0.3, integration work with the iRODS Consortium CI infrastructure, and a large number of accumulated fixes 
and features marking the transition to the iRODS consortium model.

=======
Please go to [[https://github.com/DICE-UNC/jargon]] for the latest news and info.

Jargon-core consists of the following libraries

* jargon-core - base libraries, implementation of the iRODS protocol
* jargon-data-utils - additional functionality for dealing with iRODS data, such as building trees, storing information in iRODS on behalf of applications, and doing diffs between local and iRODS
* jargon-user-tagging - code for using free tagging and other metadata metaphors on top of iRODS
* jargon-user-profile - allows management of user profile and related configuration data in a user home directory
* jargon-conveyor - transfer manager for managing and synchronizing data with iRODS
* jargon-ticket - support for ticket processing
* jargon-httpstream - stream http content into iRODS via Jargon
* jargon-ruleservice - support for running and managing rules from interfaces
* jargon-workflow - support for iRODS workflows

## Requirements

*Jargon depends on Java 1.6+
*Jargon is built using Apache Maven2, see POM for dependencies
*Jargon supports iRODS 3.0 through iRODS 3.3.1 community, as well as iRODS 4.0.3 consortium

## Libraries

Jargon-core uses Maven for dependency management.  See the pom.xml file for references to various dependencies.

Note that the following bug and feature requests are logged in GForge with related commit information [[https://github.com/DICE-UNC/jargon/issues]]

## Bug Fixes

#### fix display version of file length for rounding #54

Fix display of file size to not round down for CollectionAndDataObjectListingEntry

## Features

#### Setting inheritance on collection as admin #55

Add ability to set inherit/noinherit in CollectionAO as admin

#### Add create/mod date/data size to DataObjectAOImpl.findMetadataValuesByMetadataQuery #60

Include additional information on data size, create and modify dates to MetaDataAndDomainData,
useful in metadata query based virtual collections

