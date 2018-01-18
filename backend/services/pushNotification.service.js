'use strict';

const winston = require('winston');
const fcm = require('node-gcm');
const config = require('../config/settings.config');
const ERROR = require('../config/error.config');

let sender;

module.exports.initFcm = function() {
  return new Promise((resolve, reject) => {
    winston.debug('init fcm');
    sender = new fcm.Sender(config.fcmServerKey);
    resolve();
  });
};

module.exports.sendfcm = function(tokens, data, notification, dryRun) {
    return new Promise((resolve, reject) => {
      let responseData = {payload: {}};
      winston.debug('Sending fcm message');

      const message = new fcm.Message({
        notification: notification,
        data: data,
        dryRun: dryRun
      });

      sender.send(message, tokens, (err, res) => {
        if (err) {
          winston.error('send fcm message failed', JSON.stringify(err));
          responseData.success = false;
          responseData.payload.dataPath = 'pushNotification';
          responseData.payload.message = 'failed to send push notification via fcm';
          const errorCode = ERROR.SEND_FCM_FAILED;
          reject({errorCode: errorCode, responseData: responseData});
        } else if (!err && res.success == 0) {
          winston.debug('send fcm message failed', JSON.stringify(res));
          responseData.success = false;
          responseData.payload.dataPath = 'pushNotification';
          responseData.payload.message = 'failed to send push notification via fcm';
          const errorCode = ERROR.SEND_FCM_FAILED;
          reject({errorCode: errorCode, responseData: responseData});
        } else {
          winston.debug('send fcm message was successful', JSON.stringify(res));
          responseData.success = true;
          responseData.payload.message = 'notification sent successfully';
          resolve(responseData);
        }
      });
    });
  };
