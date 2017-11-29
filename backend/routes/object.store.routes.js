var express = require('express');
var router = express.Router();

var objectStoreController = require('../controllers/object.store.controller');

router.post('/upload', objectStoreController.upload);

module.exports = router;
