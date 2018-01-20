'use strict';

const tv4 = require('tv4');
const winston = require('winston');
const ERROR = require('../config/error.config');

function againstSchema(inputData, jsonSchema) {
  // Validate the received json data from the request with the predefined json schema
  return new Promise((resolve,reject) => {
    let responseData = {payload: {}};
    const validationResult = tv4.validate(inputData, jsonSchema);
    if (validationResult === true) {
      // validation was successful
      winston.debug('Success: json is valid');
      responseData.success = true;
      resolve(responseData);
    } else {
      // validation failed
      const refinedErrorObj = {
        dataPath: tv4.error.dataPath.replace('/', ''),
        message: tv4.error.message
      };
      winston.debug('Error: validation failed', refinedErrorObj);
      responseData.success = false;
      responseData.payload = {
        dataPath: 'validation',
        message: 'invalid json'
      };
      reject({errorCode: ERROR.INVALID_JSON, responseData: responseData});
    }
  });
}
module.exports.againstSchema = againstSchema;

module.exports.reqBodyAgainstSchema = function(inputData, jsonSchema) {
  return new Promise((resolve,reject) => {
    let responseData = {payload: {}};
    againstSchema(inputData, jsonSchema)
    .then(validationResult => {
      responseData.success = true;
      resolve(responseData);
    })
    .catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'validation';
      responseData.payload.message = 'invalid request body';
      const errorCode = ERROR.INVALID_REQUEST_BODY;
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};
