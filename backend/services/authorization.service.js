const express = require('express');

const database = require('../modules/database.module');
const group = require('../modules/group.module');
const winston = require('winston');
const httpResponseService = require('./httpResponse.service');
const ERROR = require('../config.error');

//TODO: Refactor
exports.isAuthorizedAdmin = function(req, res, next) {
  winston.debug('Authorizing request as admin');
  let admin = 'admin';
  if (isAuthenticated(req, res)) {
    let userId = res.locals.userId;
    verifyRole(userId, admin).
        then(promise => next()).
        catch((error) => {
          winston.error('Non-admin user attempted to access /groups: ' +
              userId);
          const resBody = {'success': false, 'payload': error.message};
          httpResponseService.send(res, 403, resBody);
        });
  } else {
    let error = 'Authorization failed due to missing authentication';
    let resBody = {'success': false, 'payload': error};
    httpResponseService.send(res, 500, resBody);
  }
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

//TODO: Refactor/Delete
function isAuthenticated(req, res) {
  if (res.locals.userId === undefined) {
    let errorString = 'Request on baseUrl ' + req.baseUrl + ' with path ' +
        req.path + ' cannot be authorized without prior authentication';
    winston.error(errorString);
    return false;
  } else {
    return true;
  }
}

//TODO: Refactor/Delete
function verifyRole(userId, roleId) {
  if (roleId === 'admin') {
    return Promise.reject(
        {authorizedAdmin: false, message: 'Admin access required'});
  } else {
    return Promise.resolve({authorizedAdmin: true});
  }
}

