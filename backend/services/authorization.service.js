'use strict';

const express = require('express');

const database = require('../modules/database.module');
const group = require('../modules/group.module');
const user = require('../modules/user.module');
const winston = require('winston');
const httpResponseService = require('./httpResponse.service');
const ERROR = require('../config/error.config');
const ROLES = require('../config/roles.config');

exports.isAuthorizedAdmin = function(req, res, next) {
  winston.debug('Authorizing request as admin');

  isAuthenticated(res.locals).then(authResult => {
    return user.verifyRole(authResult.payload.userId, ROLES.ADMIN);
  }).then((verifyRoleResult) => {
    winston.debug('User authorized as admin');
    next();
  }).catch(errorResult => {
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.NOT_AUTHENTICATED:
        statusCode = 401;
        break;
      case ERROR.UNKNOWN_USER:
        statusCode = 403;
        errorResult.responseData.payload.dataPath = 'authorization';
        errorResult.responseData.payload.message = 'user is not authorized';
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

exports.isGroupMember = function(req, res, next) {
  winston.debug('Hello from isGroupMember');
  let responseData = {payload: {}, success: false};
  Promise.resolve().then(() => {
    if (!res.locals.userId) {
      responseData.payload.dataPath = 'authentication';
      responseData.message = 'user is not authenticated';
      return Promise.reject({errorCode: ERROR.NOT_AUTHENTICATED, responseData: responseData});
    } else {
      return group.verifyGroupContainsUser(res.locals.userId, req.params.groupId);
    }
  }).then(verfifyResult => {
    next();
  }).catch(errorResult => {
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.MISSING_ID_IN_URL:
        statusCode = 400;
        break;
      case ERROR.NOT_AUTHENTICATED:
        statusCode = 401;
        break;
      case ERROR.USER_NOT_IN_GROUP:
        statusCode = 403;
        break;
      case ERROR.UNKNOWN_GROUP:
        statusCode = 404;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

function isAuthenticated(resLocals) {
  let responseData = {payload: {}};
  if (resLocals && 'userId' in resLocals && 'authType' in resLocals && 'authToken' in resLocals) {
    responseData.success = true;
    responseData.payload.userId = resLocals.userId;
    responseData.payload.authType = resLocals.authType;
    responseData.payload.authToken = resLocals.authToken;
    return Promise.resolve(responseData);
  } else {
    responseData.success = false;
    responseData.payload.dataPath = 'authentication';
    responseData.payload.message = 'user is not authenticated';
    let errorCode = ERROR.NOT_AUTHENTICATED;
    return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
}
