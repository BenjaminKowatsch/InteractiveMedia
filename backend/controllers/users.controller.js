'use strict';

const winston = require('winston');

const user = require('../modules/user.module');
const ROLES = require('../config/roles.config');
const ERROR = require('../config/error.config');
const AUTH_TYPE = require('../config/authType.config');

const validateJsonService = require('../services/validateJson.service');
const httpResponseService = require('../services/httpResponse.service');

const jsonSchema = {
  registerUserPayload: require('../jsonSchema/registerUserPayload.json'),
  loginUserExternalPayload: require('../jsonSchema/loginUserExternalPayload.json'),
  loginUserPasswordPayload: require('../jsonSchema/loginUserPasswordPayload.json'),
  updateUserPayload: require('../jsonSchema/updateUserPayload.json')
};

exports.registerNewUser = function(req, res) {
  validateJsonService.reqBodyAgainstSchema(req.body, jsonSchema.registerUserPayload)
  .then(validationResult => {
    return user.register(req.body.username, req.body.password, req.body.email, ROLES.USER, req.body.imageUrl);
  })
  .then(function(registerResult) {
    let responseData = {payload: {}};
    responseData.success = true;
    responseData.payload = registerResult.payload;
    httpResponseService.send(res, 201, responseData);
  }).catch(errorResult => {
    winston.error(errorResult.errorCode);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.INVALID_REQUEST_BODY:
        statusCode = 400;
        break;
      case ERROR.DUPLICATED_USER:
        statusCode = 409;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

exports.login = function(req, res) {
  // get login type from uri parameter 'type'
  const loginType = req.query.type;

  // validate request body depending on login type
  switch (Number(loginType)) {
    case AUTH_TYPE.PASSWORD: {
      winston.debug('loginType: Password');
      validateJsonService.reqBodyAgainstSchema(req.body, jsonSchema.loginUserPasswordPayload)
      .then(validationResult => {
        return user.passwordLogin(req.body.username, req.body.password);
      })
     .then(loginResult => {
        let responseData = {payload: {}};
        responseData.success = true;
        responseData.payload = loginResult.payload;
        httpResponseService.send(res, 200, responseData);
      })
      .catch(errorResult => {
        winston.error(errorResult.errorCode);
        let statusCode = 418;
        switch (errorResult.errorCode) {
          case ERROR.INVALID_REQUEST_BODY:
            statusCode = 400;
            break;
          case ERROR.RESOURCE_NOT_FOUND:
            statusCode = 401;
            errorResult.responseData.payload.dataPath = 'login';
            errorResult.responseData.payload.message = 'login failed';
            break;
          case ERROR.DB_ERROR:
          case ERROR.UNCAUGHT_ERROR:
            statusCode = 500;
            break;
        }
        httpResponseService.send(res, statusCode, errorResult.responseData);
      });
      break;
    }
    case AUTH_TYPE.GOOGLE: {
      winston.debug('loginType: GOOGLE');
      // validate data in request body
      validateJsonService.reqBodyAgainstSchema(req.body, jsonSchema.loginUserExternalPayload)
      .then(validationResult => {
        return user.verifyGoogleAccessToken(req.body.accessToken, false);
      })
      .then(tokenValidationResult => {
          winston.debug('GoogleAccessToken: is valid');
          return user.googleOrFacebookLogin(tokenValidationResult.payload.userId,
            tokenValidationResult.payload.expiryDate, AUTH_TYPE.GOOGLE, req.body.accessToken,
            tokenValidationResult.payload.email, tokenValidationResult.payload.username,
            tokenValidationResult.payload.imageUrl);
        })
      .then(loginResult => {
        let responseData = {payload: {}};
        responseData.success = true;
        responseData.payload = loginResult.payload;
        httpResponseService.send(res, 200, responseData);
      })
      .catch(errorResult => {
        winston.error(errorResult.errorCode);
        let statusCode = 418;
        switch (errorResult.errorCode) {
          case ERROR.INVALID_REQUEST_BODY:
            statusCode = 400;
            break;
          case ERROR.INVALID_AUTH_TOKEN:
            statusCode = 401;
            errorResult.responseData.payload.dataPath = 'login';
            errorResult.responseData.payload.message = 'login failed';
            break;
          case ERROR.DB_ERROR:
          case ERROR.UNCAUGHT_ERROR:
            statusCode = 500;
            break;
        }
        httpResponseService.send(res, statusCode, errorResult.responseData);
      });
      break;
    }
    case AUTH_TYPE.FACEBOOK: {
      winston.debug('loginType: FACEBOOK');
      // validate data in request body
      validateJsonService.reqBodyAgainstSchema(req.body, jsonSchema.loginUserExternalPayload)
      .then(validationResult => {
        return user.verifyFacebookAccessToken(req.body.accessToken, false, true);
      })
      .then(tokenValidationResult => {
        winston.debug('FacebookAccessToken: is valid');
        return user.googleOrFacebookLogin(tokenValidationResult.payload.userId,
          tokenValidationResult.payload.expiryDate, AUTH_TYPE.FACEBOOK, req.body.accessToken,
          tokenValidationResult.payload.email, tokenValidationResult.payload.username,
          tokenValidationResult.payload.imageUrl);
      })
      .then(loginResult => {
        let responseData = {payload: {}};
        responseData.success = true;
        responseData.payload = loginResult.payload;
        httpResponseService.send(res, 200, responseData);
      })
      .catch(errorResult => {
        winston.error(errorResult.errorCode);
        let statusCode = 418;
        switch (errorResult.errorCode) {
          case ERROR.INVALID_REQUEST_BODY:
            statusCode = 400;
            break;
          case ERROR.INVALID_AUTH_TOKEN:
            statusCode = 401;
            errorResult.responseData.payload.dataPath = 'login';
            errorResult.responseData.payload.message = 'login failed';
            break;
          case ERROR.DB_ERROR:
          case ERROR.UNCAUGHT_ERROR:
            statusCode = 500;
            break;
        }
        httpResponseService.send(res, statusCode, errorResult.responseData);
      });
      break;
    }
    default: {
      // invalid login type
      const resBody = {
        'success': false,
        'payload': {
          'dataPath': 'authType',
          'message': 'invalid auth type'
        }};
      httpResponseService.send(res, 400, resBody);
      break;
    }
  }
};

exports.logout = function(req, res) {
  user.logout(res.locals.userId, res.locals.authType)
    .then(() => {
      let responseData = {payload: {}};
      responseData.success = true;
      httpResponseService.send(res, 200, responseData);
    })
    .catch(errorResult => {
      winston.error(errorResult.errorCode);
      let statusCode = 418;
      switch (errorResult.errorCode) {
        case ERROR.DB_ERROR:
        case ERROR.UNCAUGHT_ERROR:
        case ERROR.RESOURCE_NOT_FOUND:
          statusCode = 500;
          break;
      }
      httpResponseService.send(res, statusCode, errorResult.responseData);
    });
};

exports.getUserData = function(req, res) {
  winston.debug('Hello from getUserData');
  user.getUserData(res.locals.userId).then(userResult => {
    httpResponseService.send(res, 200, userResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.UNKNOWN_USER:
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

exports.updateUser = function(req, res) {
  winston.debug('Hello from updateUser');
  validateJsonService.reqBodyAgainstSchema(req.body, jsonSchema.updateUserPayload)
  .then(() => {
    return user.updateUser(res.locals.userId, req.body);
  })
  .then(updateResult => {
    httpResponseService.send(res, 200, updateResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.INVALID_REQUEST_BODY:
        statusCode = 400;
        break;
      case ERROR.UNKNOWN_USER:
      case ERROR.RESOURCE_NOT_FOUND:
        statusCode = 500;
        errorResult.responseData.dataPath = 'user';
        errorResult.responseData.message = 'internal server error';
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};
