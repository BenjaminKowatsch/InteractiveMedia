var express = require('express');
var router = express.Router();

var versionController = require('../controllers/version.controller');

//base route: host:8081/version

router.get('/', versionController.getVersion);

module.exports = router;
