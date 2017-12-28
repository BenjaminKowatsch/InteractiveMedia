'use strict';

var express = require('express');
var router = express.Router();

var testController = require('../controllers/test.controller');
var authenticationService = require('../services/authentication.service');

router.get('/authentication/none', testController.getAuthenticationNotRequired);
router.get('/authentication/required', authenticationService.isAuthenticated, testController.getAuthenticationRequired);
module.exports = router;
