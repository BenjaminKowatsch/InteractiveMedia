const winston = require('winston');
const jwt = require('jwt-simple');
const https = require('https');
const querystring = require('querystring');
const GoogleAuth = require('google-auth-library');

const config = require('./config');
const uuidService = require('../services/uuid.service');
const tokenService = require('../services/token.service');
const database = require('../modules/database');

const MONGO_ERRCODE = {
  'DUPLICATEKEY': 11000
};
const MONGO_DB_CONNECTION_ERROR_CODE = 10;
const MONGO_DB_REQUEST_ERROR_CODE = 9;

const MONGO_DB_CONNECTION_ERROR_OBJECT = {'errorCode': MONGO_DB_CONNECTION_ERROR_CODE};

const AUTH_TYPE = {
  'PASSWORD': 0,
  'GOOGLE': 1,
  'FACEBOOK': 2
};
exports.AUTH_TYPE = AUTH_TYPE;

// 60 minutes in ms
const validTimeOfTokenInMs = 3600000;

const googleAuth = new GoogleAuth();
const googleAuthClient = new googleAuth.OAuth2(config.googleOAuthClientID, '', '');

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
    googleAuthClient.verifyIdToken(token, config.googleOAuthClientID,
    function(error, login) {
      if (error === null) {
        var payload = login.getPayload();
        var userId = payload.sub;
        var email = payload.email;
        var expiryDate = new Date(payload.exp * 1000);
        // if verifyDatabase flag is set also check if expiryDate is valid
        if (verifyDatabase === true) {
          if (undefined === userCollection) {
            winston.error('Error userCollection is not set');
            reject(MONGO_DB_CONNECTION_ERROR_OBJECT);
          } else {
            // check database
            var query = {
              'userId': userId,
              'email': email,
              'authType': AUTH_TYPE.GOOGLE,
              'expiryDate': {
                '$gte': expiryDate
              }
            };
            winston.debug('query:', query);
            var options = {fields: {userId: 1, authType: 1, email: 1, expiryDate: 1}};
            userCollection.findOne(query, options, function(error, result) {
              if (error === null && result !== null) {
                var promiseData = {
                  'expiryDate': result.expiryDate,
                  'email': result.email,
                  'userId': result.userId
                };
                winston.debug('returning:', promiseData);
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
          winston.debug('returning: ', promiseData);
          resolve(promiseData);
        }
      } else {
        winston.debug('token declared invalid by google library: ', error);
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
 * [httpsGetRequest description]
 *
 * @param  {[type]} options [description]
 * @return {[type]}         [description]
 */
function httpsGetRequest(options) {
  return new Promise((resolve, reject) => {
    winston.debug('get: https://' + options.host + options.path);
    https.get(options, function(response) {
      var responseMessage = '';

      response.on('data', function(chunk) {
        responseMessage += chunk;
      });

      response.on('end', () => {
        winston.info('Received data ' + responseMessage);
        resolve(JSON.parse(responseMessage));
      });
      response.on('error', (err) => {
        winston.info('Error data ' + err);
        reject(err);
      });
    });
  });
}

function verifyFacbookTokenAtDatabase(data, userCollection, verifyDatabase) {
  return new Promise((resolve, reject) => {
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
        winston.debug('verify database: ', query);
        var options = {fields: {userId: 1, expiryDate: 1}};
        userCollection.findOne(query, options, function(error, result) {
          if (error === null && result !== null) {
            var promiseData = {
              expiryDate: result.expiryDate,
              userId: result.userId
            };
            winston.debug('returning:', promiseData);
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
      winston.debug('returning: ', promiseData);
      if (verifyEmail === true) {
        verifyFacbookEmail(token).then((emailResult) => {
          promiseData.email = emailResult.email;
          resolve(promiseData);
        }).catch((err) => {
          reject(err);
        });
      } else {
        resolve(promiseData);
      }
    }
  });
}

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
 *                                                 {String} message String containing the error message or facebook error
 */
exports.verifyFacebookAccessToken = function(userCollection, token, verifyDatabase, verifyEmail) {
  var options = {
    host: 'graph.facebook.com',
    path: ('/v2.9/debug_token?access_token=' +
            config.facebookUrlAppToken + '&input_token=' + token)
  };
  return httpsGetRequest(options)
  .then((data) => {
    winston.debug('resolving https promise: ' + JSON.stringify(data));
    var expiryDate = new Date(data.data.expires_at * 1000);
    var userId = data.data.user_id;
    var promiseData = {
      expiryDate: expiryDate,
      userId: userId
    };
    if (verifyDatabase === true) {
      return verifyFacbookTokenAtDatabase(data, userCollection, verifyDatabase);
    } else {
      return promiseData;
    }
  }).then((result) => {
    winston.debug('resolving database promise: ' + JSON.stringify(result));
    if (verifyEmail === true) {
      var emailOptions = {
        host: 'graph.facebook.com',
        path: ('/v2.9/me?fields=name,email&access_token=' + token)
      };
      return httpsGetRequest(emailOptions)
          .then((emailResult) => {
            winston.debug('resolving email promise: ' + JSON.stringify(emailResult));
            result.email = emailResult.email;
            return result;
          });
    } else {
      return result;
    }
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
   responseData, userId, expiryDate, authType, accessToken, email) {
  return new Promise((resolve, reject) => {
    // Upsert entry at db
    userCollection.updateOne({
      'userId': userId,
      'email': email,
      'authType': authType
    }, {
      'userId': userId,
      'email': email,
      'authType': authType,
      'expiryDate': expiryDate
    }, {
      upsert: true
    },
      function(err, result) {
        if (err !== null) {
          responseData.success = false;
          winston.error('Login failed');
          reject(responseData);
        } else {
          responseData.payload = {};
          responseData.payload.authType = authType;
          responseData.payload.accessToken = accessToken;
          winston.debug('Login successful ');
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
      winston.debug('Error code: ' + MONGO_DB_CONNECTION_ERROR_CODE);
      reject(responseData);
    } else {
      var newExpiryDate = tokenService.getNewExpiryDate(validTimeOfTokenInMs);
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
          winston.debug(result.value);
          // Successfully logged in and created new expiry date
          // Generate Access Token
          // Remove the database id from the json object
          delete result.value._id;
          responseData.payload.authType = AUTH_TYPE.PASSWORD;
          responseData.payload.accessToken = jwt.encode(result.value, config.jwtSimpleSecret);
          winston.debug('Login successful ');
          resolve(responseData);
        } else {
          // Error handling
          responseData.success = false;
          winston.debug('Login failed ');
          resolve(responseData);
        }
      });
    }
  });
};
/**
 * Function to logout a user independent of authType
 *
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @return {Promise}                then: {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the successful request
 *                                                 {JSONObject} payload
 *                                  catch: {JSONObject} error Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the unsuccessful request
 *                                                 {JSONObject} payload
 */
exports.logout = function(userId, authType) {
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
    database.collections.users.updateOne(query, update, function(err, result) {
      // Parse driver result from string to json an object
      result = JSON.parse(result);
      if (err === null && result.n === 1 && result.ok === 1) {
        // update successful
        winston.debug('Logout successful');
        resolve();
      } else {
        winston.debug('Logout failed');
        reject();
      }
    });
  });
};

/**
 * Function to register a new user at the database
 *
 * @param  {String} username       The name of the new user
 * @param  {String} password       The password of the new user
 * @return {Promise}                then: {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 {Boolean} success  Flag to indicate the successful request
 *                                                 {JSONObject} payload
 *                                  catch: {JSONObject} error Is a modified version of the responseData object
 *                                                {Boolean} success  Flag to indicate the unsuccessful request
 *                                                {JSONObject} payload
 */
exports.register = function(username, password, email) {
  return new Promise((resolve, reject) => {
    const userToRegister = {
      'expiryDate': tokenService.getNewExpiryDate(validTimeOfTokenInMs),
      'password': password,
      'username': username,
      'email': email,
      'userId': uuidService.generateUUID(),
      'authType': AUTH_TYPE.PASSWORD
    };

    database.collections.users.insertOne(userToRegister, function(err, result) {
      let responseData = {
        'success': false,
        'payload': {}
      };
      if (err != null && err.code === MONGO_ERRCODE.DUPLICATEKEY) {
        // error: duplicated key
        responseData.payload.dataPath = 'username';
        responseData.payload.message = 'Username already exists';
        responseData.success = false;
        winston.error('Registration failed. Duplicated key');
        reject(responseData);
      } else {
        // update successful
        const toEncode = {
          'userId': userToRegister.userId,
          'expiryDate': userToRegister.expiryDate
        };
        responseData.payload = {
          'accessToken': tokenService.generateAccessToken(toEncode),
          'authType': AUTH_TYPE.PASSWORD
        };
        winston.debug('Registration successful');
        resolve(responseData);
      }
    });
  });
};
