const winston = require('winston');

const group = require('../modules/group');
const database = require('../modules/database');

const validateJsonService = require('../services/validateJson.service');
const httpResonseService = require('../services/httpResonse.service');

const jsonSchema = {
  groupPayloadData: require('../JSONSchema/groupPayloadData.json'),
  postData: require('../JSONSchema/postData.json')
};

exports.createNewGroup = function(req, res) {
  winston.debug('Creating a new group');
  // validate data in request body
  validateJsonService.againstSchema(req.body.payload, jsonSchema.groupPayloadData).then(validationResult => {
    return group.createNewGroup(req.body);
  }).then(registerResult =>  {
    httpResonseService.sendHttpResponse(res, registerResult.statusCode, registerResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    httpResonseService.sendHttpResponse(res, errorResult.statusCode, errorResult);
  });
};

exports.getAll = function(req, res) {
  winston.debug('Getting all groups');
  httpResonseService.sendHttpResponse(res, 404, 'Not implemented');
};

exports.getById = function(req, res) {
  const groupId = req.params.groupid;
  winston.debug('Getting group with id ' + groupId);
  httpResonseService.sendHttpResponse(res, 404, 'Not implemented');
};
