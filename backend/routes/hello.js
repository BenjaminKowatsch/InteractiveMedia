var express = require('express');
var router = express.Router();

var helloController = require('../controllers/hello.controller');

//base route: localhost:8081/hello

/**
 * @api {get} /hello
 * @apiName GetHelloWorld
 *
 * @apiSuccess {Date} date current date.
 */
router.get('/world', helloController.getHello);

module.exports = router;
