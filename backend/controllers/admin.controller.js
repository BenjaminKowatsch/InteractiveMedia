'use strict';

const winston = require('winston');
const config = require('../modules/config');
const user = require('../modules/user.module');
const group = require('../modules/group.module');
const httpResponseService = require('../services/httpResponse.service');
const ERROR = require('../config.error');
const ROLES = require('../config.roles');

module.exports.getAllGroups = function(req, res) {
  group.getAllGroups().then(groupResult => {
      let responseData = {payload: {}};
      responseData.success = true;
      responseData.payload = groupResult.payload.groups;
      httpResponseService.send(res, 200, responseData);
    }).catch(errorResult => {
      winston.error(errorResult.errorCode);
      let statusCode = 418;
      switch (errorResult.errorCode) {
        case ERROR.DB_ERROR:
          statusCode = 500;
          break;
      }
      httpResponseService.send(res, statusCode, errorResult.responseData);
    });
};

module.exports.getGroupById = function(req, res) {
  const groupId = req.params.groupId;
  group.getGroupById(groupId).then(groupResult =>  {
    let responseData = {payload: {}};
    responseData.success = true;
    responseData.payload = groupResult.payload;
    httpResponseService.send(res, 200, groupResult);
  }).catch(errorResult => {
    winston.error(errorResult.errorCode);
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

module.exports.getAllUsers = function(req, res) {
  user.getAllUsers().then(userResult => {
      let responseData = {payload: {}};
      responseData.success = true;
      responseData.payload = userResult.payload.users;
      httpResponseService.send(res, 200, responseData);
    }).catch(errorResult => {
      winston.error(errorResult.errorCode);
      let statusCode = 418;
      switch (errorResult.errorCode) {
        case ERROR.DB_ERROR:
          statusCode = 500;
          break;
      }
      httpResponseService.send(res, statusCode, errorResult.responseData);
    });
};

module.exports.getUserById = function(req, res) {
  const userId = req.params.userId;
  user.getUserData(userId).then(userResult =>  {
    let responseData = {payload: {}};
    responseData.success = true;
    responseData.payload = userResult.payload;
    httpResponseService.send(res, 200, userResult);
  }).catch(errorResult => {
    winston.error(errorResult.errorCode);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.UNKNOWN_USER:
        statusCode = 404;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};