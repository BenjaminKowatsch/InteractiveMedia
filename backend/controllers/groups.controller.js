const winston = require('winston');

const group = require('../modules/group.module');
const database = require('../modules/database.module');
const ERROR = require('../config.error');

const validateJsonService = require('../services/validateJson.service');
const httpResponseService = require('../services/httpResponse.service');

const jsonSchema = {
  groupPayloadData: require('../JSONSchema/groupPayloadData.json'),
  postData: require('../JSONSchema/postData.json')
};

exports.createNewGroup = function(req, res) {
  winston.debug('Creating a new group');
  // validate data in request body
  validateJsonService.againstSchema(req.body, jsonSchema.groupPayloadData).then(() => {
    return group.createNewGroup(res.locals.userId, req.body);
  }).then(registerResult =>  {
    httpResponseService.send(res, 201, registerResult);
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
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};

exports.getAll = function(req, res) {
  winston.debug('Getting all groups');
  httpResponseService.send(res, 404, 'Not implemented');
};

exports.getGroupById = function(req, res) {
  const groupId = req.params.groupId;
  group.getGroupById(groupId).then(groupResult =>  {
    httpResponseService.send(res, 200, groupResult);
  }).catch(errorResult => {
    winston.debug(errorResult);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.MISSING_ID_IN_URL:
        statusCode = 400;
        break;
      case ERROR.UNKNOWN_GROUP:
        statusCode = 404;
        break;
      case ERROR.UNKNOWN_USER:
      case ERROR.DB_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};
