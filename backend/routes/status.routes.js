'use strict';

const express = require('express');
const router = express.Router();

const statusController = require('../controllers/status.controller');

/**
 * @api {GET} /v1/status/ Get status
 * @apiName GetStatus
 * @apiGroup status
 * @apiVersion  0.1.0
 *
 * @apiSuccess (SuccessCode) {200} Success Status is healthy
 * @apiUse successBodySuccess
 * @apiSuccess (Success) {String} payload[status] Current status
 *
 * @apiSuccessExample {json} Success-Response:
   {
       "success": true,
       "payload": {
           "status": "healthy"
       }
   }
 *
 */
router.get('/', statusController.getStatus);

module.exports = router;
