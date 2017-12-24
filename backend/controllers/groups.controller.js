const winston = require('winston');

const group = require('../modules/group');
const database = require('../modules/database');
const ERROR = require('../config.error');

const validateJsonService = require('../services/validateJson.service');
const httpResonseService = require('../services/httpResonse.service');

const jsonSchema = {
  groupPayloadData: require('../JSONSchema/groupPayloadData.json'),
  postData: require('../JSONSchema/postData.json')
};

exports.createNewGroup = function(req, res) {
  winston.debug('Creating a new group');
  // validate data in request body
  validateJsonService.againstSchema(req.body.payload, jsonSchema.groupPayloadData).then(() => {
    return group.createNewGroup(res.locals.userId, req.body.payload);
  }).then(registerResult =>  {
    httpResonseService.sendHttpResponse(res, 201, registerResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.UNKNOWN_USER:
        statusCode = 409;
        break;
      case ERROR.INVALID_REQUEST_BODY:
      case ERROR.INVALID_CREATE_GROUP_VALUES:
        statusCode = 400;
        break;
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResonseService.sendHttpResponse(res, statusCode, errorResult.responseData);
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
