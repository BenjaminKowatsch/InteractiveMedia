var express = require('express');
var router = express.Router();

var usersController = require('../controllers/users.controller');
var authenticationService = require('../services/authentication.service');
//base route: host:8081/users

router.post('/', usersController.registerNewUser);

/**
 *
 * @api {POST} /v1/login?type=:type Login
 * @apiName Login
 * @apiGroup user
 * @apiVersion  0.1.0
 *
 * @apiParam (type) {string} type Login type with type=(0: Password, 1:Google, 2: Facebook)
 * @apiParam (password) {string} username Username
 * @apiParam (password) {string} password Hash of a password
 * @apiParam (facebook) {string} TODO TODO
 * @apiParam (google) {string} TODO TODO
 *
 * @apiSuccess (Success) {string} success Request successful
 * @apiSuccess (Success) {string} payload.authtype Login type with type=(0: Password, 1:Google, 2: Facebook)
 * @apiSuccess (Success) {string} payload.accessToken Access token for further use
 *
 * @apiError (Error) {string} success Request failed
 * @apiError (Error) {string} payload.dataPath Describe the error location
 * @apiError (Error) {string} payload.message Specify the error
 * @apiError (Error400) {String}  BadRequest Missing or malformed credentials
 * @apiError (Error400) {String}  BadRequest Missing or unknown type parameter
 * @apiError (Error401) {String}  Unauthorized Credentials are invalid
 *
 * @apiParamExample  {type} Password
    {
     "username" : "alex",
     "password" : "XHDGETFHFJCHF"
    }
 * @apiParamExample  {type} Facebook
    {
     "TODO" : "TODO",
     "TODO" : "TODO"
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

router.post('/logout', authenticationService.isAuthenticated, usersController.logout);

router.post('/sendData', authenticationService.isAuthenticated, usersController.dummyFunction);

module.exports = router;
