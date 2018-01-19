'use strict';

const tv4 = require('tv4');
const winston = require('winston');
const ERROR = require('../config/error.config');

/**
 * Function to validate the request data with a JSON schema.
 *
 * @param {JSONObject} inputData Input JSON data to be validated
 * @param {JSONObject} jsonSchema  JSON schema to validate
 * @return {JSONObject} JSON object indicating validation result and validation error in case invalid JSON
 */
exports.validateAgainstSchema = function(inputData, jsonSchema) {
  let returnValue = {
    'valid': false,
  };
  if (inputData == null) {
    returnValue = {
      'valid': false,
      'error': {
        'dataPath': 'validation',
        'message': 'null input'
      }
    };
  } else if (Object.keys(inputData).length === 0 && inputData.constructor === Object) {
    returnValue = {
      'valid': false,
      'error': {
        'dataPath': 'validation',
        'message': 'empty input'
      }
    };
  } else {
    // Validate the received json data from the request with the predefined json schema
    const validationResult = tv4.validate(inputData, jsonSchema);

    if (validationResult === true) {
      // validation was successful
      winston.debug('Success: json is valid');
      returnValue.valid = true;
    } else {
      // validation failed
      const refinedErrorObj = {
        dataPath: tv4.error.dataPath.replace('/', ''),
        message: tv4.error.message
      };
      winston.debug('Error: validation failed', refinedErrorObj);
      returnValue.valid = false;
      returnValue.error = refinedErrorObj;
    }
  }
  return returnValue;
};

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
