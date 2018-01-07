'use strict';

const winston = require('winston');

const group = require('../modules/group.module');
const user = require('../modules/user.module');
const database = require('../modules/database.module');
const ERROR = require('../config.error');

const validateJsonService = require('../services/validateJson.service');
const httpResponseService = require('../services/httpResponse.service');
const pushNotificationService = require('../services/pushNotification.service');

const jsonSchema = {
  groupPayloadData: require('../JSONSchema/groupPayloadData.json'),
  transactionPayloadData: require('../JSONSchema/transactionPayloadData.json')
};

module.exports.createNewGroup = function(req, res) {
  winston.debug('Creating a new group');
  // validate data in request body
  validateJsonService.againstSchema(req.body, jsonSchema.groupPayloadData).then(() => {
    return group.createNewGroup(res.locals.userId, req.body);
  }).then(registerResult =>  {
    httpResponseService.send(res, 201, registerResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.UNKNOWN_USER:
        statusCode = 409;
        break;
      case ERROR.INVALID_REQUEST_BODY:
      case ERROR.INVALID_CREATE_GROUP_VALUES:
        statusCode = 400;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

module.exports.getAll = function(req, res) {
  winston.debug('Getting all groups');
  httpResponseService.send(res, 404, 'Not implemented');
};

module.exports.getGroupById = function(req, res) {
  const groupId = req.params.groupId;
  group.getGroupById(groupId).then(groupResult =>  {
    httpResponseService.send(res, 200, groupResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.MISSING_ID_IN_URL:
        statusCode = 400;
        break;
      case ERROR.UNKNOWN_GROUP:
        statusCode = 404;
        break;
      case ERROR.UNKNOWN_USER:
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

module.exports.createNewTransaction = function(req, res) {
  winston.debug('Creating a new transaction');
  const groupId = req.params.groupId;
  const userIdCreator = res.locals.userId;
  // validate data in request body
  validateJsonService.againstSchema(req.body, jsonSchema.transactionPayloadData).then(() => {
    return group.createNewTransaction(groupId, req.body);
  }).then(transactionResult =>  {
    httpResponseService.send(res, 201, transactionResult);
    sendNotificationCreateTransaction(groupId, userIdCreator);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.MISSING_ID_IN_URL:
      case ERROR.UNKNOWN_GROUP:
      case ERROR.INVALID_REQUEST_BODY:
      case ERROR.INVALID_CREATE_TRANSACTION_VALUES:
      case ERROR.USER_NOT_IN_GROUP:
        statusCode = 400;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

function sendNotificationCreateTransaction(groupId, userIdCreator) {
  let transactionGroup;
  // load group to get users and group meta data
  // TODO: do not load transaction to reduce load
  group.getGroupById(groupId)
  .then(groupResult => {
    transactionGroup = groupResult.payload;
    let userIds = transactionGroup.users.map(user => {return user.userId;});
    const indexCreatorTransaction = userIds.indexOf(userIdCreator);

    if (indexCreatorTransaction != -1) {
      // remove userId of creator
      userIds.splice(indexCreatorTransaction, 1);
    } else {
      const responseData = {
        success: false,
        payload: {
          dataPath: 'notification',
          message: 'userId of creator was not found in userIds of group'
        }
      };
      const errorCode = ERROR.UNKNOWN_USER;
      Promise.reject({errorCode: errorCode, responseData: responseData});
    }

    if (userIds.length > 0) {
      // get fcm tokens of users
      return user.getFcmTokensByUserIds(userIds);
    } else {
      const responseData = {
        success: false,
        payload: {
          dataPath: 'notification',
          message: 'there are no users left to send a notification to'
        }
      };
      const errorCode = ERROR.UNKNOWN_USER;
      Promise.reject({errorCode: errorCode, responseData: responseData});
    }
  })
  .then(fcmTokenResult => {
    // compose message
    const tokens = fcmTokenResult.payload;
    const dryRun = false;
    const data = {};
    const notification = {
      title: 'New transaction in group "' + transactionGroup.name + '"',
      icon: 'ic_launcher',
      body: 'Click to see the newest transactions. // Debug: sent at ' + new Date()
    };
    return pushNotificationService.sendfcm(tokens, data, notification, dryRun);
  })
  .then(() => {
    winston.debug('Sent notification');
  })
  .catch(errorResult => {
    winston.error(errorResult);
  });
}

module.exports.getTransactionAfterDate = function(req, res) {
  const date = req.query.after;
  group.getTransactionAfterDate(req.params.groupId, date).then(transactionResult =>  {
    httpResponseService.send(res, 200, transactionResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.MISSING_ID_IN_URL:
      case ERROR.INVALID_DATE_FORMAT:
        statusCode = 400;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};
