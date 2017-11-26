var tv4 = require('tv4');
var winston = require('winston');

/**
 * Function to validate the request data with a JSON schema.
 *
 * @param {JSONObject} inputData Input JSON data to be validated
 * @param {JSONObject} jsonSchema  JSON schema to validate
 * @return {JSONObject} JSON object indicating validation result and validation error in case invalid JSON
 */
exports.validateAgainstSchema = function(inputData, jsonSchema) {
  // Validate the received json data from the request with the predefined json schema
  var validationResult = tv4.validate(inputData, jsonSchema);
  var returnValue = {};

  if (validationResult === true) {
    // validation was successful
    winston.info('Success: json is valid');
    returnValue.valid = true;
  } else {
    // validation failed
    var refinedErrorObj = {
      dataPath: tv4.error.dataPath.replace('/', ''),
      message: tv4.error.message
    };
    winston.error('Error: validation failed', refinedErrorObj);
    returnValue.valid = false;
    returnValue.error = refinedErrorObj;
  }
  return returnValue;
};