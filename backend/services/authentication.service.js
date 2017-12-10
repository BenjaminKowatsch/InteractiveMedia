var express = require('express');
var router = express.Router();

var winston = require('winston');

var user = require('../modules/user');
var database = require('../modules/database');
var validateJsonService = require('./validateJson.service');
var httpResonseService = require('./httpResonse.service');

var jsonSchema = {
  postData: require('../JSONSchema/postData.json')
};

exports.isAuthenticated = function(req, res, next) {
  winston.info('req.body', req.body);

  // validate data in request body
  var validationResult = validateJsonService.validateAgainstSchema(req.body, jsonSchema.postData);

  if (validationResult.valid === true) {
    winston.info('Request body is valid');

    // request body is valid
    verifyAccessToken(req.body.accessToken, req.body.authType)
    .then((promiseData) => {
      winston.info('VerifyAccessToken result: ' + JSON.stringify(promiseData));
      res.locals.userId = promiseData.userId;
      next();
    })
    .catch((error) => {
      // access token is invalid, unauthorized
      winston.error('Invalid access Token ');
      var resBody = {'success': false, 'payload': error};
      httpResonseService.sendHttpResponse(res, 401, resBody);
    });
  } else {
    // request body is invalid
    winston.error('Request body is invalid ');
    var resBody = {'success': false, 'payload': validationResult.error};
    httpResonseService.sendHttpResponse(res, 400, resBody);
  }
};

/**
 * Function to verify each type of access token (google, facebook or password)
 *
 * @param  {String} token    AccessToken to be verified
 * @param  {user.AUTH_TYPE} authType Authentication type specifing whether the user belongs to google, facebook or password authentication
 * @return {Promise}          then: {JSONObject} promiseData JSON object containing the following properties:
 *                                                 {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                            catch: {JSONObject} error JSON object containing the following properties:
 */
function verifyAccessToken(token, authType) {
  switch (authType) {
    case user.AUTH_TYPE.PASSWORD:
      winston.info('Verifing Password access token');
      // Verify password access token
      return user.verifyPasswordAccessToken(database.collections.users, token);
    case user.AUTH_TYPE.GOOGLE:
      winston.info('Verifing Google access token');
      // Verify google access token
      return user.verifyGoogleAccessToken(database.collections.users, token, true);
    case user.AUTH_TYPE.FACEBOOK:
      winston.info('Verifing Facebook access token');
      // Verify facebook access token
      return user.verifyFacebookAccessToken(database.collections.users, token, true);
    default:
      winston.error('Unknown authType');
      return Promise.reject('Unknown authType');
  }
}
