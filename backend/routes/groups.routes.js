'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const groupsController = require('../controllers/groups.controller');
const authenticationMiddleware = require('../middleware/authentication.middleware');
const authorizationMiddleware = require('../middleware/authorization.middleware');

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
router.post('/', authenticationMiddleware.isAuthenticated,
    groupsController.createNewGroup);

/**
 * @api {GET} /v1/groups/:groupId Get Group
 * @apiName GetGroup
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
 * @apiSuccess (SuccessCode) {200} Success Success get group
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
router.get('/:groupId', authenticationMiddleware.isAuthenticated,
    authorizationMiddleware.isGroupMember, groupsController.getGroupById);

/**
 * @api {POST} /v1/groups/:groupId/transactions Create Transaction
 * @apiName CreateTransaction
 * @apiGroup group
 * @apiVersion 0.1.0
 *
 * @apiUse paramTransactionObject
 *
 * @apiUse paramExampleCreateTransaction
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiSuccess (SuccessCode) {201} Success Transaction Created
 * @apiUse successExampleTransaction
 *
 * @apiUse successBodySuccess
 * @apiSuccess (Success) {string} payload Recevied transaction object
 * @apiSuccess (Success) {string} payload[publishedAt] Date when transaction was created in backend (ISO-8601, format: YYYY-MM-DDTHH:mm:ss.sssZ)
 *
 * @apiUse error400InvalidBody
 * @apiUse error400UserNotInGroup
 * @apiUse error400MissingUnknownUrlParameter
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/:groupId/transactions', authenticationMiddleware.isAuthenticated,
    authorizationMiddleware.isGroupMember, groupsController.createNewTransaction);

/**
 * @api {GET} /v1/groups/:groupId/transactions?after=ISOdate Get Transactions After
 * @apiName GetTransactionsAfter
 * @apiGroup group
 * @apiVersion 0.1.0
 *
 * @apiUse paramUrlGroupId
 * @apiUse paramUrlTransactionsAfterDate
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiSuccess (SuccessCode) {200} Success Success get transactions
 * @apiUse successExampleTransactions
 *
 * @apiUse error400MissingUnknownUrlParameter
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error404UnknownId
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/:groupId/transactions', authenticationMiddleware.isAuthenticated,
    authorizationMiddleware.isGroupMember, groupsController.getTransactionAfterDate);

module.exports = router;
