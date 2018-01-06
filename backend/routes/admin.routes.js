'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const adminController = require('../controllers/admin.controller');
const authenticationService = require('../services/authentication.service');
const authorizationService = require('../services/authorization.service');

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
router.get('/groups', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
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
 *
 * @apiSuccess (SuccessCode) {200} Success Group
 * @apiUse successExampleGroup
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
router.get('/groups/:groupId', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
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
router.get('/users', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
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
router.get('/users/:userId', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
    adminController.getUserById);

module.exports = router;