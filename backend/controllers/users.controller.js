var winston = require('winston');

var user = require('../modules/user');
var database = require('../modules/database');

var jsonValidator = require('../services/validateJson');
var httpResponder = require('../services/sendHttpResonse');

var jsonSchema = {
  userData: require('../JSONSchema/userData.json')
};

exports.registerNewUser = function(req, res) {

    winston.info('req.body', req.body);

    // validate data in request body
    var validationResult = jsonValidator.validateAgainstSchema(req.body, jsonSchema.userData);

    if (validationResult.valid === true) {
      // request body is valid

      // store user in mongo
      user.register(database.collections.launometerUsers, {}, req.body.username, req.body.password)
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
