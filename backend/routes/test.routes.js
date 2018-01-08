'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const testController = require('../controllers/test.controller');
const authenticationService = require('../services/authentication.service');
const authorizationService = require('../services/authorization.service');

/**
 * @api {GET} /v1/test/authentication/none Authentication not required
 * @apiName authenticationNone
 * @apiGroup test
 * @apiVersion 0.1.0
 *
 * @apiSuccess (SuccessCode) {200} Success Access granted
 * @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "hello": "open world"
        }
    }
 *
 */
router.get('/authentication/none', testController.getAuthenticationNotRequired);

/**
 * @api {GET} /v1/test/authentication/required Authentication required
 * @apiName authenticationRequired
 * @apiGroup test
 * @apiVersion 0.1.0
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiSuccess (SuccessCode) {200} Success Access granted
 * @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "hello": "authenticated world"
        }
    }
 *
 * @apiUse error401Unauthorized
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/authentication/required', authenticationService.isAuthenticated, testController.getAuthenticationRequired);

/**
 * @api {GET} /v1/test/authorization/none Authorization not required
 * @apiName authorizationNone
 * @apiGroup test
 * @apiVersion 0.1.0
 *
 * @apiSuccess (SuccessCode) {200} Success Access granted
 * @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "hello": "open world without authorization"
        }
    }
 *
 */
router.get('/authorization/none', testController.getAuthorizationNotRequired);

/**
 * @api {GET} /v1/test/authorization/admin Authorization admin required
 * @apiName authorizationAdminRequired
 * @apiGroup test
 * @apiVersion 0.1.0
 * @apiPermission admin
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiSuccess (SuccessCode) {200} Success Access granted
 * @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "hello": "authorized world as admin"
        }
    }
 *
 * @apiUse error401Unauthorized
 * @apiUse error403Forbidden
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/authorization/admin', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
    testController.getAuthorizationAdminRequired);

/**
 * @api {POST} /v1/test/notification/user Send push notification to user
 * @apiName sendPushNotificationToUser
 * @apiGroup test
 * @apiVersion 0.1.0
 * @apiDescription User is identified by auth token.
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiParam (Parameter) {string} dryRun Mock the last step to send the notification. Allowed values: true, false. Default: true.
 * @apiParamExample {JSON} Body
    {
        "dryRun" : "true"
    }
 *
 * @apiSuccess (SuccessCode) {200} Success Notification sent
 * @apiUse successExampleSuccess
 *
 * @apiUse error401Unauthorized
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse error500FcmError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/notification/user', authenticationService.isAuthenticated, testController.sendPushNotificationToUser);

module.exports = router;
