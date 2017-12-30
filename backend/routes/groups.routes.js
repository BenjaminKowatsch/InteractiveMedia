'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const groupsController = require('../controllers/groups.controller');
const authorizationService = require('../services/authorization.service');
const authenticationService = require('../services/authentication.service');

//base route: host:8081/groups

router.get('/', authenticationService.isAuthenticated,
    authorizationService.isAuthorizedAdmin, groupsController.getAll);

/**
 * @api {POST} /v1/groups/ Create
 * @apiName Create
 * @apiGroup group
 * @apiVersion 0.1.0
 *
 * @apiParam (Parameter) {string} name Name of the group
 * @apiParam (Parameter) {string} imageUrl URL to the group image (required but can be null)
 * @apiParam (Parameter) {Array[String]} users Email addresses of the group members, including creator's email
 *
 * @apiUse paramExampleCreateGroup
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiSuccess (SuccessCode) {201} Success Group Created
 * @apiUse successExampleGroup

 * @apiUse error400InvalidBody
 * @apiUse error400DuplicatedUsers
 * @apiUse error400MissingGroupCreator
 * @apiUse error401Unauthorized
 * @apiUse error409UnknownUser
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/', authenticationService.isAuthenticated,
    groupsController.createNewGroup);

/**
 * @api {GET} /v1/groups/:groupId Get by id
 * @apiName GetById
 * @apiGroup group
 * @apiVersion 0.1.0
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
router.get('/:groupId', authenticationService.isAuthenticated,
    authorizationService.isGroupMember, groupsController.getGroupById);
module.exports = router;
