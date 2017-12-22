var tv4 = require('tv4');
var winston = require('winston');
const ERROR = require('../config.error');

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
    var validationResult = tv4.validate(inputData, jsonSchema);

    if (validationResult === true) {
      // validation was successful
      winston.debug('Success: json is valid');
      returnValue.valid = true;
    } else {
      // validation failed
      var refinedErrorObj = {
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

exports.againstSchema = function(inputData, jsonSchema) {
  // Validate the received json data from the request with the predefined json schema
  return new Promise((resolve,reject) => {
    var validationResult = tv4.validate(inputData, jsonSchema);
    if (validationResult === true) {
      // validation was successful
      winston.debug('Success: json is valid');
      resolve();
    } else {
      // validation failed
      var refinedErrorObj = {
        dataPath: tv4.error.dataPath.replace('/', ''),
        message: tv4.error.message
      };
      winston.debug('Error: validation failed', refinedErrorObj);
      let responseData = {};
      responseData.success = false;
      responseData.payload = {
        dataPath: 'validation',
        message: 'Invalide body'
      };
      reject({errorCode: ERROR.INVALID_REQUEST_BODY, responseData: responseData});
    }
  });
};
