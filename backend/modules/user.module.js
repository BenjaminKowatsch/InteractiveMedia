'use strict';

const winston = require('winston');
const jwt = require('jwt-simple');
const https = require('https');
const querystring = require('querystring');
const GoogleAuth = require('google-auth-library');

const config = require('./config');
const uuidService = require('../services/uuid.service');
const tokenService = require('../services/token.service');
const database = require('../modules/database.module');
const ERROR = require('../config.error');
const ROLES = require('../config.roles');
const AUTH_TYPE = require('../config.authType');

const MONGO_ERRCODE = {
  'DUPLICATEKEY': 11000
};

// 60 minutes in ms
const validTimeOfTokenInMs = 3600000;

const googleAuth = new GoogleAuth();
const googleAuthClient = new googleAuth.OAuth2(config.googleOAuthClientID, '', '');

/**
 * Function to verify an access token from google.
 *
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
exports.verifyGoogleAccessToken = function(token, verifyDatabase) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    // verify google access token
    googleAuthClient.verifyIdToken(token, config.googleOAuthClientID,
    function(error, login) {
      if (error === null) {
        const payload = login.getPayload();
        const userId = payload.sub;
        const email = payload.email;
        const expiryDate = new Date(payload.exp * 1000);
        // if verifyDatabase flag is set also check if expiryDate is valid
        if (verifyDatabase === true) {
          // check database
          const query = {
            'userId': userId,
            'email': email,
            'authType': AUTH_TYPE.GOOGLE,
            'expiryDate': {
              '$gte': expiryDate
            }
          };
          winston.debug('query:', query);
          const options = {fields: {userId: 1, authType: 1, email: 1, expiryDate: 1}};
          database.collections.users.findOne(query, options, function(error, result) {
            if (error === null && result !== null) {
              responseData.success = true;
              responseData.payload.expiryDate = result.expiryDate;
              responseData.payload.userId = result.userId;
              responseData.payload.email = result.email;
              resolve(responseData);
            } else if (error === null && result === null) {
              let errorCode;
              responseData.success = false;
              responseData.payload.dataPath = 'user';
              responseData.payload.message = 'unknown user or expired token';
              errorCode = ERROR.UNKNOWN_USER_OR_EXPIRED_TOKEN;
              winston.error('errorCode', errorCode);
              reject({errorCode: errorCode, responseData: responseData});
            } else {
              // Unknown internal database error
              let errorCode;
              responseData.success = false;
              responseData.payload.dataPath = 'user';
              responseData.payload.message = 'unknown database error';
              errorCode = ERROR.DB_ERROR;
              winston.error('errorCode', errorCode);
              reject({errorCode: errorCode, responseData: responseData});
            }
          });
        } else {
          responseData.success = true;
          responseData.payload.expiryDate = expiryDate;
          responseData.payload.userId = userId;
          responseData.payload.email = email;
          resolve(responseData);
        }
      } else {
        // Google claims invalid token
        responseData.success = false;
        responseData.payload.dataPath = 'authentication';
        responseData.payload.message = 'invalid auth token';
        let errorCode = ERROR.INVALID_AUTH_TOKEN;
        winston.error('errorCode', errorCode);
        reject({errorCode: errorCode, responseData: responseData});
      }
    });
  });
};
/**
 * Function to verify an own access token.
 *
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: {JSONObject} promiseData Containing the following properties:
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                                  catch: {JSONObject} error Containing the following properties:
 *                                                 {String} message String containing the error message
 *                                                OR
 *                                                MongoDB Error
 */
exports.verifyPasswordAccessToken = function(token) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    tokenService.decodeToken(token).then(promiseData => {
      const query = {
        userId: promiseData.payload.userId,
        authType: AUTH_TYPE.PASSWORD,
        expiryDate: {
          '$gt': new Date()
        }
      };
      winston.debug('verifyPasswordAccessToken: query', JSON.stringify(query));
      const options = {fields: {userId: 1, expiryDate: 1}};
      database.collections.users.findOne(query, options, function(error, result) {
        if (error === null && result !== null) {
          responseData.success = true;
          responseData.payload.expiryDate = result.expiryDate;
          responseData.payload.userId = result.userId;
          resolve(responseData);
        } else if (error === null && result === null) {
          let errorCode;
          responseData.success = false;
          responseData.payload.dataPath = 'user';
          responseData.payload.message = 'unknown user or expired token';
          errorCode = ERROR.UNKNOWN_USER_OR_EXPIRED_TOKEN;
          winston.error('errorCode', errorCode);
          reject({errorCode: errorCode, responseData: responseData});
        } else {
          let errorCode;
          responseData.success = false;
          responseData.payload.dataPath = 'user';
          responseData.payload.message = 'unknown database error';
          errorCode = ERROR.DB_ERROR;
          winston.error('errorCode', errorCode);
          reject({errorCode: errorCode, responseData: responseData});
        }
      });
    }).catch((errorResult) => {
      reject(errorResult);
    });
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
      let responseMessage = '';

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

function verifyFacbookTokenAtDatabase(data, verifyDatabase) {
  let responseData = {payload: {}};
  return new Promise((resolve, reject) => {
    const expiryDate = new Date(data.data.expires_at * 1000);
    const userId = data.data.user_id;
    // if verifyDatabase flag is set also check if expiryDate is valid
    if (verifyDatabase === true) {
      // check database
      const query = {
        userId: userId,
        authType: AUTH_TYPE.FACEBOOK,
        expiryDate: {
          '$gte': expiryDate
        }
      };
      winston.debug('verify database: ', query);
      const options = {fields: {userId: 1, expiryDate: 1}};
      database.collections.users.findOne(query, options, function(error, result) {
        if (error === null && result !== null) {
          responseData.success = true;
          responseData.payload.expiryDate = result.expiryDate;
          responseData.payload.userId = result.userId;
          resolve(responseData);
        } else if (error === null && result === null) {
          let errorCode;
          responseData.success = false;
          responseData.payload.dataPath = 'user';
          responseData.payload.message = 'unknown user or expired token';
          errorCode = ERROR.UNKNOWN_USER_OR_EXPIRED_TOKEN;
          winston.error('errorCode', errorCode);
          reject({errorCode: errorCode, responseData: responseData});
        } else {
          // Unknown internal database error
          let errorCode;
          responseData.success = false;
          responseData.payload.dataPath = 'user';
          responseData.payload.message = 'unknown database error';
          errorCode = ERROR.DB_ERROR;
          winston.error('errorCode', errorCode);
          reject({errorCode: errorCode, responseData: responseData});
        }
      });
    } else {
      responseData.success = true;
      responseData.payload.expiryDate = expiryDate;
      responseData.payload.userId = userId;
      resolve(responseData);
    }
  });
}

/**
 * Function to verify a access token from facebook.
 *
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: {JSONObject} promiseData Containing the following properties:
 *                                                 {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                                  catch: {JSONObject} error Containing the following properties:
 *                                                 {String} message String containing the error message or facebook error
 */
exports.verifyFacebookAccessToken = function(token, verifyDatabase, verifyEmail) {
  let responseData = {payload: {}};
  const options = {
    host: 'graph.facebook.com',
    path: ('/v2.9/debug_token?access_token=' +
            config.facebookUrlAppToken + '&input_token=' + token)
  };
  return httpsGetRequest(options)
    .then((data) => {
      winston.debug('resolving https promise: ' + JSON.stringify(data));

      // check if Facebook verified the provided token
      if (data.data.error) {
        // Facebook claimed invalid access token
        responseData.success = false;
        responseData.payload.dataPath = 'authentication';
        responseData.payload.message = 'invalid auth token';
        let errorCode = ERROR.INVALID_AUTH_TOKEN;
        winston.error('errorCode', errorCode);
        return Promise.reject({errorCode: errorCode, responseData: responseData});
      }

      const expiryDate = new Date(data.data.expires_at * 1000);
      const userId = data.data.user_id;
      if (verifyDatabase === true) {
        return verifyFacbookTokenAtDatabase(data, verifyDatabase);
      } else {
        responseData.success = true;
        responseData.payload.expiryDate = expiryDate;
        responseData.payload.userId = userId;
        winston.debug('in verifyFacbookTokenAtDatabase verifyDatabase === false', JSON.stringify(responseData));
        return responseData;
      }
    })
    .then((result) => {
      winston.debug('resolving database promise: ' + JSON.stringify(result));
      if (verifyEmail === true) {
        const emailOptions = {
          host: 'graph.facebook.com',
          path: ('/v2.9/me?fields=name,email&access_token=' + token)
        };
        return httpsGetRequest(emailOptions)
            .then((emailResult) => {
              winston.debug('resolving email promise: ' + JSON.stringify(emailResult));
              result.payload.email = emailResult.email;
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
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @param {Date} expiryDate Date to indicate the expiration of the accessToken, will be stored into the database
 * @param  {AUTH_TYPE} authType An enumeration value, which specifies the current type of authentication,
 *                                   to be stored into the responseData, so the client will received it and store it into a cookie
 * @param  {String} accessToken    AccessToken to be stored into the responseData, so the client will received it and store it into a cookie
 * @return {Promise}                then:  {JSONObject} object containing access token and auth type
 *                                  catch:  {JSONObject} object containing an error message
 */
exports.googleOrFacebookLogin = function(userId, expiryDate, authType, accessToken, email) {
  return new Promise((resolve, reject) => {
    let responseData = {};
    // Upsert entry at db
    database.collections.users.updateOne({
      'userId': userId,
      'email': email,
      'authType': authType
    }, {
      'userId': userId,
      'email': email,
      'authType': authType,
      'expiryDate': expiryDate,
      'role': ROLES.USER
    }, {
      upsert: true
    },
      function(err, result) {
        if (err !== null) {
          responseData = {
            'dataPath': 'login',
            'message': 'login failed'
          };
          winston.error('Login failed');
          reject(responseData);
        } else {
          responseData = {
            'authType': authType,
            'accessToken': accessToken
          };
          winston.debug('Login successful ');
          resolve(responseData);
        }
      });
  });
};

/**
 * Function to login a user with password.
 *
 * @param  {String} username       The name of the user
 * @param  {String} password       The password of the user
 * @return {Promise}                then:  {JSONObject} object containing access token and auth type
 *                                  catch:  {JSONObject} object containing an error message
 */
exports.passwordLogin = function(username, password) {
  return new Promise((resolve, reject) => {
    const newExpiryDate = tokenService.getNewExpiryDate(validTimeOfTokenInMs);
    const query = {
      'username': username,
      'password': password
    };
    const update = {
      '$set': {
        'expiryDate': newExpiryDate
      }
    };
    const options = {
      projection: {
        userId: 1,
        expiryDate: 1
      },
      returnOriginal: false
    };

    database.collections.users.findOneAndUpdate(query, update, options, function(err, result) {
      let responseData = {};
      if (err === null && result.value !== null && result.ok === 1) {
        // Successfully logged in and created new expiry date
        const toEncode = {
          'userId': result.value.userId,
          'expiryDate': result.value.expiryDate
        };
        responseData = {
          'accessToken': tokenService.generateAccessToken(toEncode),
          'authType': AUTH_TYPE.PASSWORD
        };
        winston.debug('Login successful');
        resolve(responseData);
      } else {
        // Error handling
        winston.debug('Login failed');
        responseData = {
          'dataPath': 'login',
          'message': 'login failed'
        };
        reject(responseData);
      }
    });
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
    const update = {
      '$set': {
        'expiryDate': new Date()
      }
    };
    const query = {
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

exports.register = function(username, password, email, role) {
  return new Promise((resolve, reject) => {
    const userToRegister = {
      'expiryDate': tokenService.getNewExpiryDate(validTimeOfTokenInMs),
      'password': password,
      'username': username,
      'email': email,
      'role': role,
      'userId': uuidService.generateUUID(),
      'authType': AUTH_TYPE.PASSWORD
    };

    database.collections.users.insertOne(userToRegister, function(err, result) {
      let responseData = {payload: {}};
      if (err != null && err.code === MONGO_ERRCODE.DUPLICATEKEY) {
        // error: duplicated key
        responseData.success = false;
        responseData.payload.dataPath = 'register';
        responseData.payload.message = 'username already exists';
        let errorCode = ERROR.DUPLICATED_USER;
        reject({errorCode: errorCode, responseData: responseData});
      } else if (err == null && result) {
        // update successful
        const toEncode = {
          'userId': userToRegister.userId,
          'expiryDate': userToRegister.expiryDate
        };
        responseData.payload = {
          'accessToken': tokenService.generateAccessToken(toEncode),
          'authType': AUTH_TYPE.PASSWORD
        };
        winston.debug('Registration successful', responseData.payload.accessToken);
        resolve(responseData);
      } else {
        // Unknown internal database error
        winston.debug('err', JSON.stringify(err));
        let errorCode;
        responseData.success = false;
        responseData.payload.dataPath = 'user';
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
        winston.error('errorCode', errorCode);
        reject({errorCode: errorCode, responseData: responseData});
      }
    });
  });
};

module.exports.getUserData = function(userId) {
  winston.debug('Hello from module getUserData');
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    checkIfUserIdIsGiven(userId)
    .then(() => findUserById(userId))
    .then(checkIfUserResultIsNotNull)
    .then(userResult => {
      delete userResult._id;
      responseData.success = true;
      responseData.payload = userResult;
      resolve(responseData);
    }).catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'user';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

function checkIfUserIdIsGiven(userId) {
  return new Promise((resolve, reject) => {
    if (!userId) {
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'missing userId';
      errorToReturn.errorCode = ERROR.UNKNOWN_USER;
      reject(errorToReturn);
    } else {
      resolve();
    }
  });
}

function findUserById(userId) {
  let query = {userId: userId};
  let options = {fields: {username: true, groupIds: true, email: true, userId: true, role: true}};
  return database.collections.users.findOne(query, options);
}

function checkIfUserResultIsNotNull(userResult) {
  return new Promise((resolve, reject) => {
    if (!userResult) {
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'user not found';
      errorToReturn.errorCode = ERROR.UNKNOWN_USER;
      reject(errorToReturn);
    } else {
      resolve(userResult);
    }
  });
}

module.exports.verifyRole = function(userId, role) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    checkIfUserIdIsGiven(userId).then(() => {
        const query = {userId: userId, role: role};
        return database.collections.users.findOne(query);
      })
    .then(checkIfUserResultIsNotNull)
    .then(userResult => {
      responseData.success = true;
      responseData.payload.userId = userResult.userId;
      responseData.payload.role = userResult.role;
      resolve(responseData);
    }).catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'authorization';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

module.exports.getAllUsers = function() {
  winston.debug('getAllUsers');
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    const aggregation = {
      $project: {
        _id: 0,
        username: 1,
        email: 1,
        userId: 1,
        role: 1,
        countGroupIds: {'$size': {'$ifNull': ['$groupIds', []]}}
      }
    };
    database.collections.users.aggregate([aggregation]).toArray()
    .then(result => {
      responseData.payload.users = result;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'user';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

function checkIfUpdateOneWasSuccessful(resultRaw) {
  return new Promise((resolve, reject) => {
    const result = JSON.parse(resultRaw);
    if (result && result.n === 1 && result.nModified === 1 && result.ok === 1) {
      resolve(result);
    } else {
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'database error';
      errorToReturn.errorCode = ERROR.DB_ERROR;
      reject(errorToReturn);
    }
  });
}

module.exports.updateFcmToken = function(userId, fcmToken) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    checkIfUserIdIsGiven(userId).then(() => {
      const query = {'userId': userId};
      const update = {
        '$set': {
          'fcmToken': fcmToken
        }
      };
      const options = {upsert: false};
      return database.collections.users.updateOne(query, update, options);
    })
    .then(checkIfUpdateOneWasSuccessful)
    .then(updateResult => {
      responseData.success = true;
      resolve(responseData);
    })
    .catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'user';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

module.exports.getFcmTokensByUserIds = function(userIds) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    const query = {userId: {'$in': userIds}};
    const options = {fcmToken: 1, _id: 0};
    database.collections.users.find(query, options).toArray().then(result => {
      const fcmTokens = result.map(user => {return user.fcmToken;});
      responseData.success = true;
      responseData.payload = fcmTokens;
      resolve(responseData);
    })
    .catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'user';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};
