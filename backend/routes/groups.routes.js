// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const groupsController = require('../controllers/groups.controller');
const authorizationService = require('../services/authorization.service');
const authenticationService = require('../services/authentication.service');

//base route: host:8081/groups

router.get('/', authenticationService.isAuthenticated,
    authorizationService.isAuthorizedAdmin, groupsController.getAll);

router.get('/:groupid', authenticationService.isAuthenticated,
    authorizationService.isGroupMember, groupsController.getById);

/**
 * @api {POST} /v1/groups/ Create
 * @apiName Create
 * @apiGroup group
 * @apiVersion 0.1.0
 *
 * @apiParam (URL-Parameter) {string} name Name of the group
 * @apiParam (URL-Parameter) {string} imageUrl URL to the group image (required but can be null)
 * @apiParam (URL-Parameter) {Array[String]} users Email addresses of the group members, including creator's email
 *
 * @apiUse paramExampleCreateGroup
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

module.exports = router;
