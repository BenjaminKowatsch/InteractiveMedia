'use strict';

const express = require('express');
const router = express.Router();

const versionController = require('../controllers/version.controller');

/**
 * @api {GET} /v1/version/ Get version
 * @apiName GetVersion
 * @apiGroup version
 * @apiVersion  0.1.0
 *
 * @apiSuccess (SuccessCode) {200} Success Request successful
 * @apiUse successBodySuccess
 * @apiSuccess (Success) {String} payload[version] Current version
 * @apiSuccess (Success) {String} payload[name] Name of the app
 *
 * @apiSuccessExample {json} Success-Response:
   {
        "success": true,
        "payload": {
            "name": "Backend",
            "version": "0.1.0"
        }
   }
 *
 */
router.get('/', versionController.getVersion);

module.exports = router;
