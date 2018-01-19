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
  winston.debug('req.body', req.body);

  // validate data in request body
  const validationResult = validateJsonService.validateAgainstSchema(req.body, jsonSchema.registerUserPayload);

  if (validationResult.valid === true) {
    // request body is valid

    // store user in mongo
    user.register(req.body.username, req.body.password, req.body.email, ROLES.USER, req.body.imageUrl)
      .then(function(registerResult) {
        // mongo update was successful
        const resBody = {'success': true, 'payload': registerResult.payload};
        httpResponseService.send(res, 201, resBody);
      })
      .catch(function(errorResult) {
        // mongo update failed
        const resBody = {'success': false, 'payload': errorResult.responseData.payload};
        httpResponseService.send(res, 400, resBody);
      });
  } else {
    // request body is invalid
    const resBody = {'success': false, 'payload': {dataPath: 'login', message: 'invalid request body'}};
    httpResponseService.send(res, 400, resBody);
  }
};

exports.login = function(req, res) {
  winston.debug('req.body', req.body);
  // get login type from uri parameter 'type'
  const loginType = req.query.type;
  let resBody = {'success': false};
  winston.debug('loginType: ', loginType);
  // validate request body depending on login type
  switch (Number(loginType)) {
    case AUTH_TYPE.PASSWORD: {
      winston.debug('loginType: Password');
      // validate data in request body
      let validationResult = validateJsonService.validateAgainstSchema(req.body, jsonSchema.loginUserPasswordPayload);

      if (validationResult.valid === true) {
        // request body is valid
        user.passwordLogin(req.body.username, req.body.password)
         .then(function(loginResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginResult};
          httpResponseService.send(res, 200, resBody);
        })
         .catch(function(loginErrorResult) {
          // mongo update failed
          resBody = {'success': false, 'payload': loginErrorResult};
          httpResponseService.send(res, 401, resBody);
        });
      } else {
        // request body is invalid
        resBody = {'success': false, 'payload': {dataPath: 'login', message: 'invalid request body'}};
        httpResponseService.send(res, 400, resBody);
      }
      break;
    }
    case AUTH_TYPE.GOOGLE: {
      winston.debug('loginType: GOOGLE');
      // validate data in request body
      let validationResult = validateJsonService.validateAgainstSchema(req.body, jsonSchema.loginUserExternalPayload);

      if (validationResult.valid === true) {
        user.verifyGoogleAccessToken(req.body.accessToken, false)
          .then(function(tokenValidationResult) {
              winston.debug('GoogleAccessToken: is valid');
              return user.googleOrFacebookLogin(tokenValidationResult.payload.userId,
                tokenValidationResult.payload.expiryDate, AUTH_TYPE.GOOGLE, req.body.accessToken,
                tokenValidationResult.payload.email, tokenValidationResult.payload.username,
                tokenValidationResult.payload.imageUrl);
            })
          .then(function(loginResult) {
              // mongo update was successful
              resBody = {'success': true, 'payload': loginResult};
              httpResponseService.send(res, 200, resBody);
            })
          .catch(function(loginErrorResult) {
              // mongo update failed
              resBody = {'success': false, 'payload': loginErrorResult};
              httpResponseService.send(res, 401, resBody);
            });
      } else {
        // request body is invalid
        resBody = {'success': false, 'payload': validationResult.error};
        httpResponseService.send(res, 400, resBody);
      }
      break;
    }
    case AUTH_TYPE.FACEBOOK: {
      winston.debug('loginType: FACEBOOK');
      // validate data in request body
      let validationResult = validateJsonService.validateAgainstSchema(req.body, jsonSchema.loginUserExternalPayload);

      if (validationResult.valid === true) {
        user.verifyFacebookAccessToken(req.body.accessToken, false, true)
          .then(function(tokenValidationResult) {
              winston.debug('FacebookAccessToken: is valid');
              winston.debug('tokenValidationResult', JSON.stringify(tokenValidationResult));
              return user.googleOrFacebookLogin(tokenValidationResult.payload.userId,
                tokenValidationResult.payload.expiryDate, AUTH_TYPE.FACEBOOK, req.body.accessToken,
                tokenValidationResult.payload.email, tokenValidationResult.payload.username,
                tokenValidationResult.payload.imageUrl);
            })
          .then(function(loginResult) {
              // mongo update was successful
              resBody = {'success': true, 'payload': loginResult};
              httpResponseService.send(res, 200, resBody);
            })
          .catch(function(loginErrorResult) {
              // mongo update failed
              winston.info('loginErrorResult', JSON.stringify(loginErrorResult));
              resBody = {'success': false, 'payload': loginErrorResult.responseData.payload};
              httpResponseService.send(res, 401, resBody);
            });
      } else {
        // request body is invalid
        resBody = {'success': false, 'payload': validationResult.error};
        httpResponseService.send(res, 400, resBody);
      }
      break;
    }
    default: {
      // invalid login type
      resBody = {
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
  winston.debug('req.body', req.body);
  winston.debug('userId: ', res.locals.userId);
  winston.debug('authType: ', res.locals.authType);

  user.logout(res.locals.userId, res.locals.authType)
    .then(function() {
      const resBody = {'success': true, 'payload': {}};
      httpResponseService.send(res, 200, resBody);
    })
    .catch(function() {
      const resBody = {'success': true, 'payload': {}};
      httpResponseService.send(res, 400, resBody);
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
