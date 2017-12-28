'use strict';

var express = require('express');
var router = express.Router();

var testController = require('../controllers/test.controller');
var authenticationService = require('../services/authentication.service');
const authorizationService = require('../services/authorization.service');

router.get('/authentication/none', testController.getAuthenticationNotRequired);
router.get('/authentication/required', authenticationService.isAuthenticated, testController.getAuthenticationRequired);

router.get('/authorization/none', testController.getAuthorizationNotRequired);
router.get('/authorization/admin', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin,
    testController.getAuthorizationAdminRequired);
module.exports = router;
