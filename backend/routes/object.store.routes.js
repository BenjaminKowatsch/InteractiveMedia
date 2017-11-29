var express = require('express');
var multer = require('multer');
var router = express.Router();

var objectStoreController = require('../controllers/object.store.controller');

router.post('/upload', multer({storage: multer.memoryStorage()}).single('uploadField'), objectStoreController.upload);

router.get('/download', objectStoreController.download);

module.exports = router;
