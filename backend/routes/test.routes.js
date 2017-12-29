'use strict';

// jscs:disable jsDoc

var express = require('express');
var router = express.Router();

var testController = require('../controllers/test.controller');
var authenticationService = require('../services/authentication.service');
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
 * @apiUse error403Unauthorized
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/authorization/admin', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
    testController.getAuthorizationAdminRequired);
module.exports = router;
