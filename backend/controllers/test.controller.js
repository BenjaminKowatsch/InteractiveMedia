'use strict';

const winston = require('winston');
const httpResponseService = require('../services/httpResponse.service');
const pushNotificationService = require('../services/pushNotification.service');
const user = require('../modules/user.module');
const ERROR = require('../config/error.config');

module.exports.getAuthenticationNotRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'open world'}});
};

module.exports.getAuthenticationRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'authenticated world'}});
};

module.exports.getAuthorizationNotRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'open world without authorization'}});
};

module.exports.getAuthorizationAdminRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'authorized world as admin'}});
};

module.exports.sendPushNotificationToUser = function(req, res) {
  const userId = res.locals.userId;
  const dryRun = req.body.dryRun !== undefined ? req.body.dryRun : true;

  user.getFcmTokensByUserIds([userId]).then(result => {
    const fcmTokens = result.payload;
    winston.debug('fcmTokens', fcmTokens);

    const data = {
      title: 'Hello world',
      icon: 'ic_launcher',
      body: 'This is a brand new notification, sent at ' + new Date()
    };
    const notification = {};
    return pushNotificationService.sendfcm(fcmTokens, data, notification, dryRun);
  })
  .then(fcmResult => {
    const responseData = {success: true};
    httpResponseService.send(res, 200, responseData);
  })
  .catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.SEND_FCM_FAILED:
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};
