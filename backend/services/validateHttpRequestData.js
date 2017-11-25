/* TV4 JSON schema validator */
var tv4 = require('tv4');

/**
 * Function to validate the request data with a JSON schema.
 *
 * @param {Object} req Request object of the REST method
 * @param {Object} res Response object of the REST method
 * @param {JSONObject} jsonSchema  JSON schema to validate incoming request data
 * @param {function} onSuccess Callback to be called if input validation was successful
 */
exports.validateRequestData = function(req, res, jsonSchema, onSuccess) {
  // Validate the received json data from the request with the predefined json schema
  var valid = tv4.validate(req.body, jsonSchema);
  var responseData = {
    'success': valid
  };
  // Only continue if the validation was successful
  if (valid === true) {
    responseData.payload = {};
    console.log('Request data validation succeeded');
    onSuccess(responseData);
  } else {
    // Otherwise inform the client that the validation failed
    console.log('Validation failed: ' + JSON.stringify(tv4.error));
    responseData.dataPath = tv4.error.dataPath.replace('/', '');
    responseData.message = tv4.error.message;
    res.send(responseData);
  }
};
