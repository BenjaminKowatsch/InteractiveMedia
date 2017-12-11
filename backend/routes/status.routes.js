var express = require('express');
var router = express.Router();

var statusController = require('../controllers/status.controller');

/**
 * @api {GET} /v1/status/ Get current status
 * @apiName GetStatus
 * @apiGroup status
 * @apiVersion  0.1.0
 *
 * @apiSuccess (200) {String} success Request successful
 * @apiSuccess (200) {String} payload.status Current status
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
