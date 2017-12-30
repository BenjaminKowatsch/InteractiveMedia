'use strict';

const winston = require('winston');
const config = require('../modules/config');
const user = require('../modules/user.module');
const group = require('../modules/group.module');
const httpResponseService = require('../services/httpResponse.service');
const ERROR = require('../config.error');
const ROLES = require('../config.roles');

module.exports.addAdmin = function(req, res) {
    let responseData = {payload: {}};
    user.register(config.adminUsername, config.adminPassword, config.adminEmail, ROLES.ADMIN).then(registerResult => {
        winston.info('register admin successful');
        responseData.success = true;
        responseData.payload.accessToken = registerResult.payload.accessToken;
        responseData.payload.authType = registerResult.payload.authType;
        httpResponseService.send(res, 201, responseData);
      }).catch(errorResult => {
        winston.error(JSON.stringify(errorResult));
        let statusCode = 418;
        switch (errorResult.errorCode) {
          case ERROR.DUPLICATED_USER:
            // admin already exists
            statusCode = 422;
            break;
          case ERROR.DB_ERROR:
            statusCode = 500;
            break;
        }
        httpResponseService.send(res, statusCode, errorResult.responseData);
      });
  };

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
