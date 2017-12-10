var winston = require('winston');
var jwt = require('jwt-simple');
var https = require('https');
var querystring = require('querystring');
var GoogleAuth = require('google-auth-library');

var config = require('./config');
var uuidService = require('../services/uuid.service');

var MONGO_DB_CONNECTION_ERROR_CODE = 10;
var MONGO_DB_REQUEST_ERROR_CODE = 9;

var MONGO_DB_CONNECTION_ERROR_OBJECT = {'errorCode': MONGO_DB_CONNECTION_ERROR_CODE};

const AUTH_TYPE = {
  'PASSWORD': 0,
  'GOOGLE': 1,
  'FACEBOOK': 2
};

exports.AUTH_TYPE = AUTH_TYPE;

var auth = new GoogleAuth();
var client = new auth.OAuth2(config.googleOAuthClientID, '', '');

/**
 * Gets a date one hour from now
 *
 * @return {Date} A date one hour in the future
 */
function getNewTokenExpiryDate() {
  var time = new Date().getTime();
  time += 3600000;
  return new Date(time);
}

/**
 * Function to verify an access token from google.
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: {JSONObject} promiseData Containing the following properties:
 *                                                 {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                                  catch:{JSONObject} error Containing the following properties:
 *                                                 {String} message String containing the error message
 *                                                OR
 *                                                Google Error
 */
exports.verifyGoogleAccessToken = function(userCollection, token, verifyDatabase) {
  return new Promise((resolve, reject) => {
    // verify google access token
    client.verifyIdToken(token, config.googleOAuthClientID,
    function(error, login) {
      if (error === null) {
        var payload = login.getPayload();
        var userId = payload.sub;
        var expiryDate = new Date(payload.exp * 1000);
        // if verifyDatabase flag is set also check if expiryDate is valid
        if (verifyDatabase === true) {
          if (undefined === userCollection) {
            winston.error('Error userCollection is not set');
            reject(MONGO_DB_CONNECTION_ERROR_OBJECT);
          } else {
            // check database
            var query = {
              userId: userId,
              authType: AUTH_TYPE.GOOGLE,
              expiryDate: {
                '$gte': expiryDate
              }
            };
            winston.info('query:', query);
            var options = {fields: {userId: 1, authType: 1, expiryDate: 1}};
            userCollection.findOne(query, options, function(error, result) {
              if (error === null && result !== null) {
                var promiseData = {
                  expiryDate: result.expiryDate,
                  userId: result.userId
                };
                winston.info('returning:', promiseData);
                resolve(promiseData);
              } else {
                // Invalid expiryDate or internal database error
                winston.error('Error MONGO_DB_INTERNAL_ERROR');
                reject(error);
              }
            });
          }
        } else {
          var promiseData = {
            expiryDate: expiryDate,
            userId: userId
          };
          winston.info('returning: ', promiseData);
          resolve(promiseData);
        }
      } else {
        winston.info('token declared invalid by google library: ', error);
        reject(error);
      }
    });
  });
};
/**
 * Function to verify an own access token.
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: {JSONObject} promiseData Containing the following properties:
 *                                                 {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                                  catch: {JSONObject} error Containing the following properties:
 *                                                 {String} message String containing the error message
 *                                                OR
 *                                                MongoDB Error
 */
exports.verifyPasswordAccessToken = function(userCollection, token) {
  return new Promise((resolve, reject) => {
    if (undefined === userCollection) {
      winston.error('Error MONGO_DB_CONNECTION_ERROR_OBJECT');
      reject(MONGO_DB_CONNECTION_ERROR_OBJECT);
    } else {
      var payload = jwt.decode(token, config.jwtSimpleSecret);
      var query = {
        userId: payload.userId,
        authType: AUTH_TYPE.PASSWORD,
        expiryDate: {
          '$gt': new Date()
        }
      };
      var options = {fields: {userId: 1, expiryDate: 1}};
      userCollection.findOne(query, options, function(error, result) {
        if (error === null && result !== null) {
          var promiseData = {
            //  userCollection: userCollection,
            expiryDate: result.expiryDate,
            userId: result.userId
          };
          resolve(promiseData);
        } else {
          winston.error('Error MONGO_DB_INTERNAL_ERROR');
          reject(error);
        }
      });
    }
  });
};
/**
 * Function to verify a access token from facebook.
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: {JSONObject} promiseData Containing the following properties:
 *                                                 {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                                  catch: {JSONObject} error Containing the following properties:
 *                                                 {String} message String containing the error message
 *                                                OR
 *                                                Facebook Error
 */
exports.verifyFacebookAccessToken = function(userCollection, token, verifyDatabase) {
  return new Promise((resolve, reject) => {
    var options = {
      host: 'graph.facebook.com',
      path: ('/v2.9/debug_token?access_token=' +
             config.facebookUrlAppToken + '&input_token=' + token)
    };
    winston.info('verifing: https://' + options.host + options.path);
    https.get(options, function(response) {
      var responseMessage = '';

      response.on('data', function(chunk) {
        responseMessage += chunk;
      });

      response.on('end', function() {
        var data = JSON.parse(responseMessage);
        if (data.error !== undefined) {
          winston.error('Received error response from facebook ');
          reject(data.error);
        } else {
          var expiryDate = new Date(data.data.expires_at * 1000);
          var userId = data.data.user_id;
          // if verifyDatabase flag is set also check if expiryDate is valid
          if (verifyDatabase === true) {
            if (undefined === userCollection) {
              winston.error('usercollection is not set ');
              reject(MONGO_DB_CONNECTION_ERROR_OBJECT);
            } else {
              // check database
              var query = {
                userId: userId,
                authType: AUTH_TYPE.FACEBOOK,
                expiryDate: {
                  '$gte': expiryDate
                }
              };
              winston.info('verify database: ', query);
              var options = {fields: {userId: 1,  expiryDate: 1}};
              userCollection.findOne(query, options, function(error, result) {
                if (error === null && result !== null) {
                  var promiseData = {
                    expiryDate: result.expiryDate,
                    userId: result.userId
                  };
                  winston.info('returning:', promiseData);
                  resolve(promiseData);
                } else {
                  // Invalid expiryDate or internal database error
                  winston.error('Error MONGO_DB_INTERNAL_ERROR: ', error);
                  reject(error);
                }
              });
            }
          } else {
            var promiseData = {
              expiryDate: expiryDate,
              userId: userId
            };
            winston.info('returning: ', promiseData);
            resolve(promiseData);
          }
        }
      });
    });
  });
};

/**
 * Function to login either a google or a facebook user.
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 *                                  Will be used to save the result from this login function.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @param {Date} expiryDate Date to indicate the expiration of the accessToken, will be stored into the database
 * @param  {user.AUTH_TYPE} authType An enumeration value, which specifies the current type of authentication,
 *                                   to be stored into the responseData, so the client will received it and store it into a cookie
 * @param  {String} accessToken    AccessToken to be stored into the responseData, so the client will received it and store it into a cookie
 * @return {Promise}                then:  {JSONObject} promiseData Is a modified version of the responseData object
 *                                                  {Boolean} success  Flag to indicate the successful request
 *                                                  {JSONObject} payload
 *                                  catch:  {JSONObject} error Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the unsuccessful request
 *                                                 {JSONObject} payload
 */
exports.googleOrFacebookLogin = function(userCollection,
   responseData, userId, expiryDate, authType, accessToken) {
  return new Promise((resolve, reject) => {
    // Upsert entry at db
    userCollection.updateOne({
      'userId': userId,
      'authType': authType
    }, {
      'userId': userId,
      'authType': authType,
      'expiryDate': expiryDate
    }, {
      upsert: true
    },
      function(err, result) {
        if (err !== null) {
          responseData.success = false;
          console.log('Login failed');
          reject(responseData);
        } else {
          responseData.payload = {};
          responseData.payload.authType = authType;
          responseData.payload.accessToken = accessToken;
          console.log('Login successful ');
          resolve(responseData);
        }
      });
  });
};

/**
 * Function to login a user with password.
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 *                                  Will be used to save the result from this login function.
 * @param  {String} username       The name of the user
 * @param  {String} password       The password of the user
 * @return {Promise}                then: {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the successful request
 *                                                 {JSONObject} payload
 *                                  catch:  {JSONObject} error Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the unsuccessful request
 *                                                 {JSONObject} payload
 */
exports.passwordLogin = function(userCollection, responseData, username, password) {
  return new Promise((resolve, reject) => {
    if (undefined === userCollection) {
      responseData.success = false;
      responseData.errorCode = MONGO_DB_CONNECTION_ERROR_CODE;
      console.log('Error code: ' + MONGO_DB_CONNECTION_ERROR_CODE);
      reject(responseData);
    } else {
      var newExpiryDate = getNewTokenExpiryDate();
      var query = {
        'username': username,
        'password': password
      };
      var update = {
        '$set': {
          'expiryDate': newExpiryDate
        }
      };
      var options = {
        projection: {
          userId: 1,
          expiryDate: 1
        },
        returnOriginal: false
      };

      userCollection.findOneAndUpdate(query, update, options, function(err, result) {
        if (err === null && result.value !== null && result.ok === 1) {
          responseData.payload = {};
          console.log(result.value);
          // Successfully logged in and created new expiry date
          // Generate Access Token
          // Remove the database id from the json object
          delete result.value._id;
          responseData.payload.authType = AUTH_TYPE.PASSWORD;
          responseData.payload.accessToken = jwt.encode(result.value, config.jwtSimpleSecret);
          console.log('Login successful ');
          resolve(responseData);
        } else {
          // Error handling
          responseData.success = false;
          console.log('Login failed ');
          resolve(responseData);
        }
      });
    }
  });
};
/**
 * Function to logout a user independent of the authType
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @return {Promise}                then: {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the successful request
 *                                                 {JSONObject} payload
 *                                  catch: {JSONObject} error Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the unsuccessful request
 *                                                 {JSONObject} payload
 */
exports.logout = function(userCollection, responseData, userId, authType) {
  return new Promise((resolve, reject) => {
    var update = {
      '$set': {
        'expiryDate': new Date()
      }
    };
    var query = {
      'userId': userId,
      'authType': authType
    };
    userCollection.updateOne(query, update, function(err, result) {
      // Driver returns result as json string, not an object, so the json string has to be parsed into an object
      result = JSON.parse(result);
      if (err === null && result.n === 1 && result.ok === 1) {
        // Successfully updated the expiryDate
        console.log('Logout successful');
        resolve(responseData);
      } else {
        responseData.success = false;
        console.log('Logout failed ');
        reject(responseData);
      }
    });
  });
};
/**
 * Function to register a new user at the database
 *
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param  {String} username       The name of the new user
 * @param  {String} password       The password of the new user
 * @return {Promise}                then: {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the successful request
 *                                                 {JSONObject} payload
 *                                  catch: {JSONObject} error Is a modified version of the responseData object
 *                                                {Boolean} success  Flag to indicate the unsuccessful request
 *                                                {Number} errorCode  Enumeration to specify the error
 *                                                {JSONObject} payload
 */
exports.register = function(userCollection, responseData, username, password) {
  return new Promise((resolve, reject) => {
    var userData = {};
    if (undefined === userCollection) {
      responseData.success = false;
      responseData.errorCode = MONGO_DB_CONNECTION_ERROR_CODE;
      console.log('Error code: ' + MONGO_DB_CONNECTION_ERROR_CODE);
      reject(responseData);
    } else {
      console.log('User will be created');
      // Setup userData
      userData.expiryDate = getNewTokenExpiryDate(); // now + 1h
      userData.password = password;
      userData.username = username;
      userData.userId = uuidService.generateUUID();
      userData.authType = AUTH_TYPE.PASSWORD;

      userCollection.insertOne(userData, function(err, result) {
        responseData.payload = {};
        if (err != null && err.code === 11000) {
          responseData.payload.dataPath = 'username';
          responseData.payload.message = 'Username already exists';
          responseData.success = false;

          console.log('Registration/Login failed ');
          reject(responseData);
        } else {
          responseData.payload = {};
          var payload = {
            'userId': userData.userId,
            'expiryDate': userData.expiryDate
          };

          responseData.payload.accessToken = jwt.encode(payload, config.jwtSimpleSecret);
          responseData.payload.authType = AUTH_TYPE.PASSWORD;
          console.log('Registration/Login successful');
          resolve(responseData);
        }
      });
    }
  });
};
