var html = module.exports = {}

/* Application configuration */
var config = require('./config')
var bodyParser = require('body-parser')/* Express */
var express = require('express')
var app = express()

/* TV4 JSON schema validator */
var tv4 = require('tv4')

var server
/**
 * Initializes the express instance with the bodyParser and sets the access control allow headers to enable CORS
 */
html.init = function () {
  /* Express settings */
  app.use(bodyParser.json())
  // Add headers
  app.use(function (req, res, next) {
    // Website you wish to allow to connect
    res.setHeader('Access-Control-Allow-Origin', config.origin)
    // Request methods you wish to allow
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE')
    // Request headers you wish to allow
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type')
    // Set to true if you need the website to include cookies in the requests sent
    // to the API (e.g. in case you use sessions)
    res.setHeader('Access-Control-Allow-Credentials', true)
    // Pass to next layer of middleware
    next()
  })
}
/**
 * Starts the http server and prints out the host and the port
 */
html.startServer = function () {
  server = app.listen(config.port, function () {
    var host = server.address().address
    var port = server.address().port
    console.log('Launometer listening at http://%s:%s', host, port)
  })
}
/**
 * Function returns a callback handler for sending a responseData object
 * @param  {Object} res   The response object of the REST method
 * @return {function}     A callback function to be used for sending a responseData object
 */
html.getSendResponseDataCallback = function (res) {
  return function (responseData) {
    console.log('Sending: ' + JSON.stringify(responseData))
    res.send(responseData)
  }
}
/**
 * Function to register a POST method with integrated request data validation
 * @param  {string} path Path to register REST function
 * @param  {JSONObject} jsonSchema JSON schema to validate incoming request data
 * @param  {function} onSuccessCallback Callback to be called if input validation and verification of the accessToken were successful
 *         @param {Object} req Request object of the REST method
 *         @param {Object} res Response object of the REST method
 *         @param {JSONObject} responseData Data object created during the request data validation containing the result
 *         @param  {user.AUTH_TYPE} authType An enumeration value, which specifies the current type of authentication
 * @param  {user.AUTH_TYPE} [staticAuthType=null] Optional value, if set it will override the authType in the request
 *                                                This value is used when registering the google and facebook login handlers,
 *                                                because in those cases the request data does not contain a authentication type
 */
html.registerPostMethodWithInputValidation = function (path, jsonSchema, onSuccessCallback, staticAuthType = null) {
  app.post(path, function (req, res) {
    console.log('Post path: ' + path + ' data: ' + JSON.stringify(req.body))
    validateRequestData(req, res, jsonSchema, function (responseData) {
      // Use static authType if set
      var authType = (staticAuthType !== null) ? staticAuthType : req.body.authType
      onSuccessCallback(res, req, responseData, authType)
    })
  })
}
/**
 * Function to validate the request data with a JSON schema.
 * @param {Object} req Request object of the REST method
 * @param {Object} res Response object of the REST method
 * @param {JSONObject} jsonSchema  JSON schema to validate incoming request data
 * @param {function} onSuccess Callback to be called if input validation was successful
 */
function validateRequestData (req, res, jsonSchema, onSuccess) {
  // Validate the received json data from the request with the predefined json schema
  var valid = tv4.validate(req.body, jsonSchema)
  var responseData = {
    'success': valid
  }
  // Only continue if the validation was successful
  if (valid === true) {
    responseData.payload = {}
    console.log('Request data validation succeeded')
    onSuccess(responseData)
  } else {
    // Otherwise inform the client that the validation failed
    console.log('Validation failed: ' + JSON.stringify(tv4.error))
    responseData.dataPath = tv4.error.dataPath.replace('/', '')
    responseData.message = tv4.error.message
    res.send(responseData)
  }
}
