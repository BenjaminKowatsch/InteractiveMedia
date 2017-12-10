const winston = require('winston');

const group = require('../modules/group');
const database = require('../modules/database');

const validateJsonService = require('../services/validateJson.service');
const httpResonseService = require('../services/httpResonse.service');

const jsonSchema = {
  groupData: require('../JSONSchema/groupData.json')
};

exports.createNewGroup = function(req, res) {
  winston.info('Creating a new group');
  // validate data in request body
  var validationResult = validateJsonService.validateAgainstSchema(req.body.payload, jsonSchema.groupData);

  if (validationResult.valid === true) {
    // request body is valid

    // create new group
    group.createNewGroup(database.collections.groups, req.body.payload)
        .then(function(registerResult) {
          // mongo update was successful
          var resBody = {'success': true, 'payload': registerResult.payload};
          httpResonseService.sendHttpResponse(res, 201, resBody);
        })
        .catch(function(registerResult) {
          // mongo update failed
          var resBody = {'success': false, 'payload': registerResult.payload};
          httpResonseService.sendHttpResponse(res, 400, resBody);
        });
  } else {
    // request body is invalid
    var resBody = {'success': false, 'payload': validationResult.error};
    httpResonseService.sendHttpResponse(res, 400, resBody);
  }
};

exports.getAll = function(req, res) {
  winston.info('Getting all groups');
  httpResonseService.sendHttpResponse(res, 404, 'Not implemented');
};

exports.getById = function(req, res) {
  const groupId = req.params.groupid;
  winston.info('Getting group with id ' + groupId);
  httpResonseService.sendHttpResponse(res, 404, 'Not implemented');
};
