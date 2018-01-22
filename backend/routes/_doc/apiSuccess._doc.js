'use strict';

// jscs:disable jsDoc

/**
 * @apiDefine successBodySuccess
    @apiSuccess (Success) {string} success Request successful
 */
/**
 * @apiDefine successBodyAuthtype
    @apiSuccess (Success) {string} payload[authtype] Login type with type=(0: Password, 1:Google, 2: Facebook)
 */
/**
 * @apiDefine successBodyAuthtoken
    @apiSuccess (Success) {string} payload[accessToken] Access token for further use
 */
/**
 * @apiDefine successBodyUserUsername
    @apiSuccess (Success) {string} payload[username] Name of user
 */
/**
 * @apiDefine successBodyUserEmail
    @apiSuccess (Success) {string} payload[email] Email of user
 */
/**
 * @apiDefine successBodyUserUserId
    @apiSuccess (Success) {string} payload[userId] Id of user
 */
/**
 * @apiDefine successBodyUserRole
    @apiSuccess (Success) {string} payload[role] Role of user. Supported roles: user, admin
 */
/**
 * @apiDefine successBodyUserAuthType
    @apiSuccess (Success) {int} payload[authType] Authentication type of user. Supported types (0: Password, 1:Google, 2: Facebook)
 */
/**
 * @apiDefine successBodyUserGroupIds
    @apiSuccess (Success) {Array[string]} payload[groupIds] Ids of groups the user belongs to
 */
/**
 * @apiDefine successBodyUserImageUrl
    @apiSuccess (Success) {string} payload[imageUrl] Url of user's profile image
 */
/**
 * @apiDefine successBodyUserCountGroupIds
    @apiSuccess (Success) {int} payload[countGroupIds] Number of groups the user belongs to
 */
/**
 * @apiDefine successBodyGroupName
    @apiSuccess (Success) {string} payload[name] Name of group
 */
/**
 * @apiDefine successBodyGroupImageUrl
    @apiSuccess (Success) {string} payload[imageUrl] ImageUrl of group
 */
/**
 * @apiDefine successBodyGroupId
    @apiSuccess (Success) {string} payload[groupId] Id of group
 */
/**
 * @apiDefine successBodyGroupCreatedAt
    @apiSuccess (Success) {string} payload[createdAt] Date when group was created
 */
/**
 * @apiDefine successBodyGroupCountTransactions
    @apiSuccess (Success) {int} payload[countTransactions] Number of transaction in group
 */
/**
 * @apiDefine successBodyGroupCountUsers
    @apiSuccess (Success) {int} payload[countUsers] Number of users in group
 */
/**
 * @apiDefine successBodyGroupTransactions
    @apiSuccess (Success) {Array[transaction]} payload[transactions] List of transactions in group
 */
/**
 * @apiDefine successBodyGroupUsers
    @apiSuccess (Success) {Array[user]} payload[users] List of users in group
 */
/**
 * @apiDefine successBodyGroupUsersUserId
    @apiSuccess (Success) {string} users[userId] Id of user
 */
/**
 * @apiDefine successBodyGroupUsersName
    @apiSuccess (Success) {string} users[username] Name of user
 */
/**
 * @apiDefine successBodyGroupUserEmail
    @apiSuccess (Success) {string} users[email] Email of user
 */
/**
 * @apiDefine successBodyGroupUserImageUrl
    @apiSuccess (Success) {string} users[imageUrl] Url of user's profile image
 */
