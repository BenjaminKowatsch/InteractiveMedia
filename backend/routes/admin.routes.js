'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const adminController = require('../controllers/admin.controller');
const authenticationMiddleware = require('../middleware/authentication.middleware');
const authorizationMiddleware = require('../middleware/authorization.middleware');

/**
 * @api {GET} /v1/admin/groups/ Get groups
 * @apiName GetGroups
 * @apiGroup admin
 * @apiVersion 0.1.0
 * @apiPermission admin
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse successBodySuccess
 * @apiUse successBodyGroupName
 * @apiUse successBodyGroupImageUrl
 * @apiUse successBodyGroupId
 * @apiUse successBodyGroupCreatedAt
 * @apiUse successBodyGroupCountUsers
 * @apiUse successBodyGroupCountTransactions
 *
 * @apiSuccess (SuccessCode) {200} Success AllGroups
 * @apiUse successExampleAdminGetAllGroups
 *
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/groups', authenticationMiddleware.isAuthenticated, authorizationMiddleware.isAuthorizedAdmin,
    adminController.getAllGroups);

/**
 * @api {GET} /v1/admin/groups/:groupId Get group by id
 * @apiName GetGroupById
 * @apiGroup admin
 * @apiVersion 0.1.0
 * @apiPermission admin
 *
 * @apiUse paramUrlGroupId
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse successBodySuccess
 * @apiUse successBodyGroupName
 * @apiUse successBodyGroupImageUrl
 * @apiUse successBodyGroupId
 * @apiUse successBodyGroupCreatedAt
 * @apiUse successBodyGroupTransactions
 * @apiUse successBodyGroupUsers
 * @apiUse successBodyGroupUsersUserId
 * @apiUse successBodyGroupUsersName
 * @apiUse successBodyGroupUserEmail
 * @apiUse successBodyGroupUserImageUrl
 *
 * @apiSuccess (SuccessCode) {200} Success Group
 * @apiUse successExampleGetGroupById
 *
 * @apiUse error400MissingUnknownUrlParameter
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error404UnknownId
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse error500UnknownUser
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/groups/:groupId', authenticationMiddleware.isAuthenticated, authorizationMiddleware.isAuthorizedAdmin,
    adminController.getGroupById);

/**
 * @api {GET} /v1/admin/users/ Get users
 * @apiName GetUsers
 * @apiGroup admin
 * @apiVersion 0.1.0
 * @apiPermission admin
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse successBodySuccess
 * @apiUse successBodyUserUsername
 * @apiUse successBodyUserEmail
 * @apiUse successBodyUserUserId
 * @apiUse successBodyUserRole
 * @apiUse successBodyUserAuthType
 * @apiUse successBodyUserCountGroupIds
 *
 * @apiSuccess (SuccessCode) {200} Success AllUsers
 * @apiUse successExampleAdminGetAllUsers
 *
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/users', authenticationMiddleware.isAuthenticated, authorizationMiddleware.isAuthorizedAdmin,
    adminController.getAllUsers);

/**
 * @api {GET} /v1/admin/users/:userId Get user by id
 * @apiName GetUserById
 * @apiGroup admin
 * @apiVersion 0.1.0
 * @apiPermission admin
 *
 * @apiUse paramUrlUserId
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse successBodySuccess
 * @apiUse successBodyUserUsername
 * @apiUse successBodyUserEmail
 * @apiUse successBodyUserUserId
 * @apiUse successBodyUserRole
 * @apiUse successBodyUserGroupIds
 * @apiUse successBodyUserImageUrl
 *
 * @apiSuccess (SuccessCode) {200} Success User
 * @apiUse successExampleUser
 *
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error404UnknownId
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/users/:userId', authenticationMiddleware.isAuthenticated, authorizationMiddleware.isAuthorizedAdmin,
    adminController.getUserById);

/**
 * @api {PUT} /v1/admin/users/:userId Update user by id
 * @apiName PutUserById
 * @apiGroup admin
 * @apiVersion 0.1.0
 * @apiPermission admin
 * @apiDescription Update user. Empty request body is not allowed.
 *
 * @apiUse paramUrlUserId
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse paramUsernameOptional
 * @apiUse paramHashedPasswordOptional
 * @apiUse paramEmailOptional
 * @apiUse paramImageUrlOptional
 * @apiUse paramFcmTokenOptional
 * @apiUse paramRoleOptional
 *
 * @apiUse paramExampleAdminUserUpdate
 *
 * @apiUse successBodySuccess
 * @apiSuccess (SuccessCode) {200} Success Update successful
 *
 * @apiUse successExampleSuccess
 *
 * @apiUse error400InvalidBody
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error404UnknownId
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.put('/users/:userId', authenticationMiddleware.isAuthenticated, authorizationMiddleware.isAuthorizedAdmin,
    adminController.updateUserById);

module.exports = router;
