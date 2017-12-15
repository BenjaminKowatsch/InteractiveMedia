var express = require('express');
var router = express.Router();

var usersController = require('../controllers/users.controller');
var authenticationService = require('../services/authentication.service');

/**
 * @apiDefine errorResponseCommonStructure
    @apiError (Error) {string} success Request failed
    @apiError (Error) {string} payload.dataPath Describe the error location
    @apiError (Error) {string} payload.message Specify the error
 */
/**
 * @apiDefine paramAccessToken
    @apiParam (type) {string} accessToken Token to verify user
 */
/**
 * @apiDefine successSuccess
    @apiSuccess (Success) {string} success Request successful
 */
/**
 * @apiDefine error400MissingUnknownUrlParameterType
    @apiError (ErrorCode) {400} MissingType Missing or unknown type parameter in url
 */
/**
 * @apiDefine error400BadRequest
    @apiError (ErrorCode) {400} BadRequest Missing or malformed request body
 */
/**
 * @apiDefine error401AccessTokenInvalid
    @apiError (ErrorCode) {401} InvalidToken Access token is invalid
 */
/**
 * @apiDefine error401CredentialsInvalid
    @apiError (ErrorCode) {401} InvalidCredentials Credentials are invalid
 */

router.post('/', usersController.registerNewUser);

/**
 * @api {POST} /v1/login?type=:type Login
 * @apiName Login
 * @apiGroup user
 * @apiVersion 0.1.0
 *
 * @apiParam (URL-Parameter) {string} type Login type with type=(0: Password, 1:Google, 2: Facebook)
 * @apiParam (password) {string} username Username
 * @apiParam (password) {string} password Hash of a password
 * @apiParam (facebook) {string} accessToken Access token requested from by Facebook after successful login
 * @apiParam (google) {string} TODO TODO
 *
 * @apiUse successSuccess
 * @apiSuccess (Success) {string} payload.authtype Login type with type=(0: Password, 1:Google, 2: Facebook)
 * @apiSuccess (Success) {string} payload.accessToken Access token for further use
 *
 * @apiUse errorResponseCommonStructure
 * @apiUse error400BadRequest
 * @apiUse error400MissingUnknownUrlParameterType
 * @apiUse error401CredentialsInvalid
 *
 * @apiParamExample  {type} Password
    {
     "username" : "alex",
     "password" : "XHDGETFHFJCHF"
    }
 * @apiParamExample  {type} Facebook
    {
     "accessToken" : "DeUBZBpAr9KCBZB3knwe1eGvcur"
    }
 * @apiParamExample  {type} Google
    {
     "TODO" : "TODO",
     "TODO" : "TODO"
    }
 * @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "authType": 0,
            "authToken": "KDFBDICBIUDSBSDhdddhf784gG7F"
        }
    }
 *
 * @apiErrorExample {json} Error example:
    {
        "success": false,
        "payload": {
            "dataPath": "login",
            "message": "login failed"
        }
    }
 */
router.post('/login', usersController.login);

/**
 * @api {POST} /v1/logout Logout
 * @apiName Logout
 * @apiGroup user
 * @apiVersion 0.1.0
 *
 * @apiUse paramAccessToken
 * @apiParam (type) {int} authType Login type with authType=(0: Password, 1:Google, 2: Facebook)
 *
 * @apiUse successSuccess
 *
 * @apiUse errorResponseCommonStructure
 * @apiUse error400BadRequest
 * @apiUse error401AccessTokenInvalid
 *
 * @apiParamExample  {type} Password
    {
     "accessToken" : "SDSHDHCS",
     "authType" : 0
    }
 * @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "authType": 0,
            "authToken": "KDFBDICBIUDSBSDhdddhf784gG7F"
        }
    }
 *
 * @apiErrorExample {json} Error example:
    {
        "success": false,
        "payload": {
            "dataPath": "logout",
            "message": "logout failed"
        }
    }
 */
router.post('/logout', authenticationService.isAuthenticated, usersController.logout);

router.post('/sendData', authenticationService.isAuthenticated, usersController.dummyFunction);

module.exports = router;
