'use strict';

// jscs:disable jsDoc

const express = require('express');
const router = express.Router();

const usersController = require('../controllers/users.controller');
const authenticationService = require('../services/authentication.service');

/**
 * @api {POST} /v1/users Register
 * @apiName Register
 * @apiGroup user
 * @apiVersion 0.1.0
 *
 * @apiUse paramUsername
 * @apiUse paramHashedPassword
 * @apiUse paramEmail
 * @apiUse paramImageUrlRequiredButNull
 *
 * @apiUse paramExampleRegister
 *
 * @apiSuccess (SuccessCode) {201} Created Resource created
 * @apiUse successBodySuccess
 * @apiUse successBodyAuthtype
 * @apiUse successBodyAuthtoken
 *
 * @apiUse successExampleAuthtypeToken
 *
 * @apiUse error400BadRequest
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/', usersController.registerNewUser);

/**
 * @api {POST} /v1/users/login?type=:type Login
 * @apiName Login
 * @apiGroup user
 * @apiVersion 0.1.0
 *
 * @apiParam (URL-Parameter) {string} type Login type with type=(0: Password, 1:Google, 2: Facebook)
 * @apiParam (Parameter: type=password) {string} username Username
 * @apiParam (Parameter: type=password) {string} password Hash of a password
 * @apiParam (Parameter: type=facebook) {string} accessToken Access token requested from by Facebook after successful login
 * @apiParam (Parameter: type=google) {string} TODO TODO
 *
 * @apiUse paramExamplePassword
 * @apiUse paramExampleLoginFacebook
 * @apiUse paramExampleLoginGoogle
 *
 * @apiSuccess (SuccessCode) {200} Success Login Successful
 * @apiUse successBodySuccess
 * @apiUse successBodyAuthtype
 * @apiUse successBodyAuthtoken
 *
 * @apiUse successExampleAuthtypeToken
 *
 * @apiUse error400BadRequest
 * @apiUse error400MissingUnknownUrlParameterType
 * @apiUse error401CredentialsInvalid
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/login', usersController.login);

/**
 * @api {POST} /v1/users/logout Logout
 * @apiName Logout
 * @apiGroup user
 * @apiVersion 0.1.0
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse successBodySuccess
 * @apiSuccess (SuccessCode) {200} Success Logout successful
 *
 * @apiUse errorBodyCommonStructure
 * @apiUse error400BadRequest
 * @apiUse error401Unauthorized
 *
 * @apiUse successExampleSuccess
 *
 * @apiUse errorExampleCommon
 */
router.post('/logout', authenticationService.isAuthenticated, usersController.logout);

/**
 * @api {GET} /v1/users/user Get User
 * @apiName getUser
 * @apiGroup user
 * @apiVersion 0.1.0
 * @apiDescription Get information about the user identified by the provided auth token.
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
 * @apiSuccess (SuccessCode) {200} Success Get User
 *
 * @apiUse successExampleUser
 *
 * @apiUse error401Unauthorized
 * @apiUse error418UncaughtError
 * @apiUse error500DatabaseError
 * @apiUse error500UnknownUser
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/user', authenticationService.isAuthenticated, usersController.getUserData);

/**
 * @api {PUT} /v1/users/user Update user
 * @apiName putUser
 * @apiGroup user
 * @apiVersion 0.1.0
 * @apiDescription Update user. User is identified by auth token.
 *
 * Empty request body is not allowed.
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 *
 * @apiUse paramUsernameOptional
 * @apiUse paramHashedPasswordOptional
 * @apiUse paramEmailOptional
 * @apiUse paramImageUrlOptional
 * @apiUse paramFcmTokenOptional
 *
 * @apiUse paramExampleUserUpdate
 *
 * @apiUse successBodySuccess
 * @apiSuccess (SuccessCode) {200} Success Update successful
 *
 * @apiUse successExampleSuccess
 *
 * @apiUse error400InvalidBody
 * @apiUse error401Unauthorized
 * @apiUse error418UncaughtError
 * @apiUse error500InternalServerError
 * @apiUse error500DatabaseError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.put('/user', authenticationService.isAuthenticated, usersController.updateUser);

module.exports = router;
