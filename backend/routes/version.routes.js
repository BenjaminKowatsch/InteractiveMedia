var express = require('express');
var router = express.Router();

var versionController = require('../controllers/version.controller');

/**
 * @api {GET} /v1/version/ Get version
 * @apiName GetVersion
 * @apiGroup version
 * @apiVersion  0.1.0
 *
 * @apiSuccess (SuccessCode) {200} Success Request successful
 * @apiSuccess (Success) {String} version Current version
 * @apiSuccess (Success) {String} name Name of the app
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
