var express = require('express');
var router = express.Router();

var winston = require('winston');

var user = require('../modules/user');
var validateJsonService = require('./validateJson.service');
var httpResonseService = require('./httpResonse.service');

var jsonSchema = {
  postData: require('../JSONSchema/postData.json')
};

module.exports.isAuthenticated = function(req, res, next) {
  let resErrorBody = {
    'success': false,
    'payload': {
      'dataPath': 'validation',
      'message': ''
    }
  };

  if ('authType' in req.body) {
    const parsedAuthType = parseInt(req.body.authType);
    if (!isNaN(parsedAuthType)) {
      req.body.authType = parsedAuthType;

      //TODO: Refactor
      // validate data in request body
      var validationResult = validateJsonService.validateAgainstSchema(req.body, jsonSchema.postData);

      if (validationResult.valid === true) {
        winston.debug('Request body is valid');

        // request body is valid
        verifyAccessToken(req.body.accessToken, req.body.authType)
        .then((promiseData) => {
          winston.debug('VerifyAccessToken result: ' + JSON.stringify(promiseData));
          res.locals.userId = promiseData.userId;
          next();
        })
        .catch((error) => {
          // access token is invalid, unauthorized
          winston.debug('Invalid access Token');
          resErrorBody.payload.dataPath = 'token';
          resErrorBody.payload.message = 'invalid access token';
          httpResonseService.sendHttpResponse(res, 401, resErrorBody);
        });
      } else {
        // request body is invalid
        winston.debug('invalid request body');
        resErrorBody.payload.message = 'invalid request body';
        httpResonseService.sendHttpResponse(res, 400, resErrorBody);
      }
    } else {
      // failed to convert authType to int
      winston.debug('invalid authType');
      resErrorBody.payload.message = 'invalid authType';
      httpResonseService.sendHttpResponse(res, 400, resErrorBody);
    }
  } else {
    // authType is missing to request body
    winston.debug('missing authType');
    resErrorBody.payload.message = 'missing authType';
    httpResonseService.sendHttpResponse(res, 400, resErrorBody);
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
      winston.error('Unknown authType');
      return Promise.reject('Unknown authType');
  }
}
