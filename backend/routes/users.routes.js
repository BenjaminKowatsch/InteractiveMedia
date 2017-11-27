var express = require('express');
var router = express.Router();

var usersController = require('../controllers/users.controller');
var authenticationService = require('../services/authenticationService');
//base route: host:8081/users

router.post('/', usersController.registerNewUser);

router.post('/login', usersController.login);

router.post('/logout', authenticationService.isAuthenticated, usersController.logout);

router.post('/sendData', authenticationService.isAuthenticated, usersController.dummyFunction);

module.exports = router;
