var express = require('express');
var router = express.Router();

var versionController = require('../controllers/version.controller');

/**
 * @api {GET} /v1/version/ Get current version
 * @apiName GetVersion
 * @apiGroup version
 * @apiVersion  0.1.0
 *
 * @apiSuccess (200) {String} name Name of app
 * @apiSuccess (200) {String} version Current version
 *
 * @apiSuccessExample {json} Success-Response:
   {
       "name": "Backend",
       "version": "0.0.1"
   }
 *
 */
router.get('/', versionController.getVersion);

module.exports = router;
