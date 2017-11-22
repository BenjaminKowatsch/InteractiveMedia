var express = require('express');
var router = express.Router();

var helloController = require('../controllers/hello.controller');

//base route: localhost:8081/hello

// GET
router.get('/world', helloController.getHello);

module.exports = router;
