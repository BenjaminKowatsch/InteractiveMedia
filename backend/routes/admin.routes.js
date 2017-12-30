'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const adminController = require('../controllers/admin.controller');
const authenticationService = require('../services/authentication.service');
const authorizationService = require('../services/authorization.service');

router.post('/add', adminController.addAdmin);

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

module.exports = router;
