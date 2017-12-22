var express = require('express');
var router = express.Router();

var winston = require('winston');

var user = require('../modules/user');
var httpResonseService = require('./httpResonse.service');

const ERROR = require('../config.error');

module.exports.isAuthenticated = function(req, res, next) {
  let authType;
  let authToken;
  const authHeaderRaw = req.get('Authorization');
  parseAuthHeader(authHeaderRaw).then(auth => {
    authType = auth. authType;
    authToken = auth.authToken;
    return verifyAccessToken(authToken, authType);
  }).then(promiseData => {
    // verified user successfully
    res.locals.userId = promiseData.userId;
    res.locals.authType = authType;
    res.locals.authToken = authToken;
    winston.debug('VerifyAccessToken result: ' + JSON.stringify(promiseData));
    next();
  }).catch((error) => {
    let resErrorBody = {
      success: false,
      payload: {
        dataPath: 'validation',
        message: ''
      }
    };
    if (error && error.isSelfProvided) {
      resErrorBody.payload.message = error.msg;
    } else if (error === ERROR.INVALID_AUTHTYPE) {
      // auth token or type invalid, unauthorized
      resErrorBody.payload.dataPath = 'authType';
      resErrorBody.payload.message = 'invalid auth type';
    } else {
      resErrorBody.payload.dataPath = 'authToken';
      resErrorBody.payload.message = 'invalid auth token';
    }
    winston.debug(resErrorBody.payload.message);
    httpResonseService.sendHttpResponse(res, 401, resErrorBody);
  });
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

function parseAuthHeader(authHeaderRaw) {
  if (authHeaderRaw === undefined) {
    // no header Authorization provided
    let msg = 'no header Authorization provided';
    return Promise.reject({isSelfProvided: true, msg: msg});
  }
  const authHeader = authHeaderRaw.split(' ');
  if (authHeader.length !== 2) {
    // invalid number arguments in header Authorization
    let msg = 'invalid number of arguments provided in header Authorization';
    return Promise.reject({isSelfProvided: true, msg: msg});
  }
  authType = parseInt(authHeader[0]);
  authToken = authHeader[1];
  if (!Number.isInteger(authType)) {
    // authType is not an integer
    let msg = 'invalid format of authType provided in header Authorization';
    return Promise.reject({isSelfProvided: true, msg: msg});
  }
  winston.debug('authType', authType);
  winston.debug('authToken', authToken);
  return Promise.resolve({authToken: authToken, authType: authType});
}
