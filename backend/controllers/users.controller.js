var winston = require('winston');

var user = require('../modules/user');
var database = require('../modules/database');

var jsonValidator = require('../services/validateJson');
var httpResponder = require('../services/sendHttpResonse');

var jsonSchema = {
  userData: require('../JSONSchema/userData.json'),
  googleFacebookLogin: require('../JSONSchema/googleFacebookLogin.json')
};

exports.registerNewUser = function(req, res) {
  winston.info('req.body', req.body);

  // validate data in request body
  var validationResult = jsonValidator.validateAgainstSchema(req.body, jsonSchema.userData);

  if (validationResult.valid === true) {
    // request body is valid

    // store user in mongo
    user.register(database.collections.users, {}, req.body.username, req.body.password)
      .then(function(registerResult) {
        // mongo update was successful
        var resBody = {'success': true, 'payload': registerResult.payload};
        httpResponder.sendHttpResponse(res, 201, resBody);
      })
      .catch(function(registerResult) {
        // mongo update failed
        var resBody = {'success': false, 'payload': registerResult.payload};
        httpResponder.sendHttpResponse(res, 400, resBody);
      });
  } else {
    // request body is invalid
    var resBody = {'success': false, 'payload': validationResult.error};
    httpResponder.sendHttpResponse(res, 400, resBody);
  }
};

exports.dummyFunction = function(req, res) {
  winston.info('req.body', req.body);

  winston.info('userId: ', res.locals.userId);
  winston.info('authType: ', req.body.authType);

  var resBody = {'success': true, 'payload': {}};
  httpResponder.sendHttpResponse(res, 201, resBody);
};

exports.login = function(req, res) {
  winston.info('req.body', req.body);
  // get login type from uri parameter 'type'
  var loginType = req.query.type;
  var validationResult = {valid: false};
  var resBody = {'success': false};
  winston.info('loginType: ', loginType);
  // validate request body depending on login type
  switch (Number(loginType)) {
    case user.AUTH_TYPE.LAUNOMETER: {
      winston.info('loginType: LAUNOMETER');
      // validate data in request body
      validationResult = jsonValidator.validateAgainstSchema(req.body, jsonSchema.userData);

      if (validationResult.valid === true) {
        // request body is valid
        user.launometerLogin(database.collections.users, {},
         req.body.username, req.body.password)
         .then(function(loginResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginResult.payload};
          httpResponder.sendHttpResponse(res, 201, resBody);
        })
         .catch(function(loginErrorResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginErrorResult};
          httpResponder.sendHttpResponse(res, 201, resBody);
        });
      } else {
        // request body is invalid
        resBody = {'success': false, 'payload': validationResult.error};
        httpResponder.sendHttpResponse(res, 400, resBody);
      }
      break;
    }
    case user.AUTH_TYPE.GOOGLE: {
      winston.info('loginType: GOOGLE');
      // validate data in request body
      validationResult = jsonValidator.validateAgainstSchema(req.body, jsonSchema.googleFacebookLogin);

      if (validationResult.valid === true) {
        user.verifyGoogleAccessToken(database.collections.users, req.body.accessToken, false)
      .then(function(tokenValidationResult) {
          winston.info('GoogleAccessToken: is valid');
          return user.googleOrFacebookLogin(database.collections.users, {}, tokenValidationResult.userId,
                       tokenValidationResult.expiryDate, user.AUTH_TYPE.GOOGLE, req.body.accessToken);
        })
      .then(function(loginResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginResult.payload};
          httpResponder.sendHttpResponse(res, 201, resBody);
        })
      .catch(function(loginErrorResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginErrorResult};
          httpResponder.sendHttpResponse(res, 201, resBody);
        });
      } else {
        // request body is invalid
        resBody = {'success': false, 'payload': validationResult.error};
        httpResponder.sendHttpResponse(res, 400, resBody);
      }
      break;
    }
    case user.AUTH_TYPE.FACEBOOK: {
      winston.info('loginType: FACEBOOK');
      // validate data in request body
      validationResult = jsonValidator.validateAgainstSchema(req.body, jsonSchema.googleFacebookLogin);

      if (validationResult.valid === true) {
        user.verifyFacebookAccessToken(database.collections.users, req.body.accessToken, false)
      .then(function(tokenValidationResult) {
          winston.info('FacebookAccessToken: is valid');
          return user.googleOrFacebookLogin(database.collections.users, {}, tokenValidationResult.userId,
                       tokenValidationResult.expiryDate, user.AUTH_TYPE.FACEBOOK, req.body.accessToken);
        })
      .then(function(loginResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginResult.payload};
          httpResponder.sendHttpResponse(res, 201, resBody);
        })
      .catch(function(loginErrorResult) {
          // mongo update was successful
          resBody = {'success': true, 'payload': loginErrorResult};
          httpResponder.sendHttpResponse(res, 201, resBody);
        });
      } else {
        // request body is invalid
        resBody = {'success': false, 'payload': validationResult.error};
        httpResponder.sendHttpResponse(res, 400, resBody);
      }
      break;
    }
    default: {
      // invalid login type
      resBody = {'success': false, 'payload': 'invalid login type'};
      httpResponder.sendHttpResponse(res, 400, resBody);
      break;
    }
  }
};

exports.logout = function(req, res) {
  winston.info('req.body', req.body);

  winston.info('userId: ', res.locals.userId);
  winston.info('authType: ', req.body.authType);

  user.logout(database.collections.users, {},
    res.locals.userId, req.body.authType)
    .then(function() {
      var resBody = {'success': true, 'payload': {}};
      httpResponder.sendHttpResponse(res, 201, resBody);
    })
    .catch(function() {
      var resBody = {'success': true, 'payload': {}};
      httpResponder.sendHttpResponse(res, 400, resBody);
    });
};
