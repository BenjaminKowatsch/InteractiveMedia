var express = require('express');
var router = express.Router();

var statusController = require('../controllers/status.controller');

//base route: host:8081/status

router.get('/', statusController.getStatus);

module.exports = router;
