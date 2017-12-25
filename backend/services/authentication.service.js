var express = require('express');
var router = express.Router();

var winston = require('winston');

var user = require('../modules/user');
var httpResponseService = require('./httpResponse.service');

const ERROR = require('../config.error');

module.exports.isAuthenticated = function(req, res, next) {
  let authType;
  let authToken;
  const authHeaderRaw = req.get('Authorization');
  parseAuthHeader(authHeaderRaw).then(auth => {
    authType = auth.payload.authType;
    authToken = auth.payload.authToken;
    return verifyAccessToken(authToken, authType);
  }).then(promiseData => {
    // verified user successfully
    res.locals.userId = promiseData.payload.userId;
    res.locals.authType = authType;
    res.locals.authToken = authToken;
    winston.debug('VerifyAccessToken result: ' + JSON.stringify(promiseData));
    next();
  }).catch((errorResult) => {
    winston.error('errorCode', errorResult.errorCode);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.NO_AUTH_HEADER:
      case ERROR.INVALID_AUTH_HEADER:
      case ERROR.INVALID_AUTHTYPE:
      case ERROR.INVALID_AUTH_TOKEN:
      case ERROR.UNKNOWN_USER_OR_EXPIRED_TOKEN:
        statusCode = 401;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
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
      let responseData = {payload: {}};
      responseData.success = false;
      responseData.payload.dataPath = 'authentication';
      responseData.payload.message = 'invalid authType provided in http request header Authorization';
      let errorCode = ERROR.INVALID_AUTHTYPE;
      winston.error('errorCode', errorCode);
      return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
}

function parseAuthHeader(authHeaderRaw) {
  let responseData = {payload: {}};
  if (authHeaderRaw === undefined) {
    // no header Authorization provided
    responseData.success = false;
    responseData.payload.dataPath = 'authentication';
    responseData.payload.message = 'no http request header Authorization provided';
    let errorCode = ERROR.NO_AUTH_HEADER;
    return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
  const authHeader = authHeaderRaw.split(' ');
  if (authHeader.length !== 2) {
    // invalid number arguments in header Authorization
    responseData.success = false;
    responseData.payload.dataPath = 'authentication';
    responseData.payload.message = 'invalid number of arguments provided in http request header Authorization';
    let errorCode = ERROR.INVALID_AUTH_HEADER;
    return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
  const authType = parseInt(authHeader[0]);
  const authToken = authHeader[1];
  if (!Number.isInteger(authType)) {
    // authType is not an integer
    responseData.success = false;
    responseData.payload.dataPath = 'authentication';
    responseData.payload.message = 'invalid authType provided in http request header Authorization';
    let errorCode = ERROR.INVALID_AUTHTYPE;
    return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
  winston.debug('authType', authType);
  winston.debug('authToken', authToken);
  responseData.success = true;
  responseData.payload.authType = authType;
  responseData.payload.authToken = authToken;
  winston.debug('parseAuthHeader: before Promise.resolve');
  return Promise.resolve(responseData);
}
