var express = require('express');
var router = express.Router();

var usersController = require('../controllers/users.controller');

//base route: host:8081/users

router.post('/', usersController.registerNewUser);

module.exports = router;
