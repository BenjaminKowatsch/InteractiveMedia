var express = require('express');
var router = express.Router();

var winston = require('winston');

var user = require('../modules/user');
var httpResonseService = require('./httpResonse.service');

const ERROR = require('../config.error');

module.exports.isAuthenticated = function(req, res, next) {

  let resErrorBody = {
    'success': false,
    'payload': {
      'dataPath': 'validation',
      'message': ''
    }
  };

  const authHeaderRaw = req.get('Authorization');

  if (authHeaderRaw !== undefined) {
    const authHeader = authHeaderRaw.split(' ');

    if (authHeader.length === 2) {
      const authType = parseInt(authHeader[0]);
      const authToken = authHeader[1];

      if (Number.isInteger(authType)) {
        winston.debug('authType', authType);
        winston.debug('authToken', authToken);
        verifyAccessToken(authToken, authType).then((promiseData) => {
            // verified user successfully
            winston.debug('VerifyAccessToken result: ' + JSON.stringify(promiseData));
            res.locals.userId = promiseData.userId;
            res.locals.authType = authType;
            res.locals.authToken = authToken;
            next();
          })
          .catch((error) => {
            // auth token or type invalid, unauthorized
            if (error === ERROR.INVALID_AUTHTYPE) {
              winston.debug('invalid auth type');
              resErrorBody.payload.dataPath = 'authType';
              resErrorBody.payload.message = 'invalid auth type';
            } else {
              resErrorBody.payload.dataPath = 'authToken';
              resErrorBody.payload.message = 'invalid auth token';
            }
            httpResonseService.sendHttpResponse(res, 401, resErrorBody);
          });
      } else {
        // authType is not an integer
        winston.debug('invalid format of authType provided in header Authorization');
        resErrorBody.payload.message = 'invalid format of authType provided in header Authorization';
        httpResonseService.sendHttpResponse(res, 401, resErrorBody);
      }
    } else {
      // invalid number arguments in header Authorization
      winston.debug('invalid number of arguments provided in header Authorization');
      resErrorBody.payload.message = 'invalid number of arguments provided in header Authorization';
      httpResonseService.sendHttpResponse(res, 401, resErrorBody);
    }
  } else {
    // no header Authorization provided
    winston.debug('no header Authorization provided');
    resErrorBody.payload.message = 'no header Authorization provided';
    httpResonseService.sendHttpResponse(res, 401, resErrorBody);
  }
};

/**
 * Function to verify each type of access token (google, facebook or password)
 *
 * @param  {String} token    AccessToken to be verified
 * @param  {user.AUTH_TYPE} authType Authentication type specifing whether the user belongs to google, facebook or password authentication
 * @return {Promise}          then: {JSONObject} promiseData JSON object containing the following properties:
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                            catch: {JSONObject} error JSON object containing the following properties:
 */
function verifyAccessToken(token, authType) {
  switch (authType) {
    case user.AUTH_TYPE.PASSWORD:
      winston.debug('Verifing Password access token');
      // Verify password access token
      return user.verifyPasswordAccessToken(token);
    case user.AUTH_TYPE.GOOGLE:
      winston.debug('Verifing Google access token');
      // Verify google access token
      return user.verifyGoogleAccessToken(token, true);
    case user.AUTH_TYPE.FACEBOOK:
      winston.debug('Verifing Facebook access token');
      // Verify facebook access token
      return user.verifyFacebookAccessToken(token, true, false);
    default:
      winston.error(ERROR.INVALID_AUTHTYPE);
      return Promise.reject(ERROR.INVALID_AUTHTYPE);
  }
}
