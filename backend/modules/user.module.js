'use strict';

const winston = require('winston');
const jwt = require('jwt-simple');
const https = require('https');
const querystring = require('querystring');
const GoogleAuth = require('google-auth-library');

const config = require('../config/settings.config');
const uuidService = require('../services/uuid.service');
const tokenService = require('../services/token.service');
const emailService = require('../services/email.service');
const mongoUtilService = require('../services/mongoUtil.service');
const database = require('../modules/database.module');
const ERROR = require('../config/error.config');
const ROLES = require('../config/roles.config');
const AUTH_TYPE = require('../config/authType.config');

const MONGO_ERRCODE = {
  'DUPLICATEKEY': 11000
};

// 60 minutes in ms
const validTimeOfTokenInMs = 3600000;

const googleAuth = new GoogleAuth();
const googleAuthClient = new googleAuth.OAuth2(config.googleOAuthClientID, '', '');

module.exports.verifyGoogleAccessToken = function(token, verifyDatabase) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    // verify google access token
    googleAuthClient.verifyIdToken(token, config.googleOAuthClientID,
    function(error, login) {
      if (error === null) {
        const payload = login.getPayload();
        const userId = payload.sub;
        const email = payload.email;
        const username = payload.name;
        const imageUrl = payload.picture;
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
          const options = {fields: {userId: 1, authType: 1, email: 1, expiryDate: 1, username: 1}};
          database.collections.users.findOne(query, options, function(error, result) {
            if (error === null && result !== null) {
              responseData.success = true;
              responseData.payload.expiryDate = result.expiryDate;
              responseData.payload.userId = result.userId;
              responseData.payload.email = result.email;
              responseData.payload.username = result.username;
              responseData.payload.imageUrl = result.imageUrl;
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
          responseData.payload.username = username;
          responseData.payload.imageUrl = imageUrl;
          resolve(responseData);
        }
      } else {
        // Google claims invalid token
        responseData.success = false;
        responseData.payload.dataPath = 'authentication';
        responseData.payload.message = 'invalid authToken';
        let errorCode = ERROR.INVALID_AUTH_TOKEN;
        winston.error('errorCode', errorCode);
        reject({errorCode: errorCode, responseData: responseData});
      }
    });
  });
};

module.exports.verifyPasswordAccessToken = function(token) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    tokenService.decodeToken(token, config.jwtSimpleSecret).then(promiseData => {
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

module.exports.verifyFacebookAccessToken = function(token, verifyDatabase, getUserInfo) {
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
        responseData.payload.message = 'invalid authToken';
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
      if (getUserInfo === true) {
        const graphOptions = {
          host: 'graph.facebook.com',
          path: ('/v2.9/me?fields=name,email,picture&access_token=' + token)
        };
        return httpsGetRequest(graphOptions)
            .then((graphResult) => {
              let username = graphResult.name;
              if (username == undefined || username == null) {
                responseData.success = false;
                responseData.payload.dataPath = 'authentication';
                responseData.payload.message = 'unable to query username from facebook';
                let errorCode = ERROR.INVALID_AUTH_TOKEN;
                return Promise.reject({errorCode: errorCode, responseData: responseData});
              } else {
                let email = graphResult.email;
                if (email == undefined || email == null) {
                  // generate own facebook email
                  email = emailService.generateFacebookEmailFromUsername(result.payload.userId, username);
                }
                result.payload.email = email;
                result.payload.username = username;
                // result.payload.imageUrl = graphResult.picture.data.url;
                result.payload.imageUrl = 'https://graph.facebook.com/v2.11/' + result.payload.userId +
                  '/picture?type=normal';
                return result;
              }
            })
            .catch(error => {
              responseData.success = false;
              responseData.payload.dataPath = 'authentication';
              responseData.payload.message = 'unable to query facebook';
              let errorCode = ERROR.INVALID_AUTH_TOKEN;
              return Promise.reject({errorCode: errorCode, responseData: responseData});
            });
      } else {
        return result;
      }
    });
};

module.exports.googleOrFacebookLogin = function(userId, expiryDate, authType, accessToken, email, username, imageUrl) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    // Upsert entry at db
    const query = {
      'userId': userId,
      'email': email,
      'authType': authType
    };
    const update = {
      '$set': {
        'userId': userId,
        'email': email,
        'authType': authType,
        'expiryDate': expiryDate,
        'role': ROLES.USER,
        'username': username,
        'imageUrl': imageUrl
      }
    };
    const options = {
      upsert: true
    };
    database.collections.users.updateOne(query, update, options)
    .then(mongoUtilService.checkIfUpdateOneUpsertWasSuccessful)
    .then(updateResult => {
      responseData.success = true;
      responseData.payload = {
            'authType': authType,
            'accessToken': accessToken
          };
      winston.debug('Login successful ');
      resolve(responseData);
    })
    .catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'login';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'uncaught error';
        errorCode = ERROR.UNCAUGHT_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

module.exports.passwordLogin = function(username, password) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
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

    database.collections.users.findOneAndUpdate(query, update, options)
    .then(mongoUtilService.checkIfFindOneAndUpdateWasSuccessful)
    .then(updateResult => {
      const toEncode = {
        'userId': updateResult.value.userId,
        'expiryDate': updateResult.value.expiryDate
      };
      responseData.success = true;
      responseData.payload = {
        'accessToken': tokenService.generateAccessToken(toEncode, config.jwtSimpleSecret),
        'authType': AUTH_TYPE.PASSWORD
      };
      winston.debug('Login successful');
      resolve(responseData);
    })
    .catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'login';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'uncaught error';
        errorCode = ERROR.UNCAUGHT_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

module.exports.logout = function(userId, authType) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    const query = {
      'userId': userId,
      'authType': authType
    };
    const update = {
      '$set': {
        'expiryDate': new Date()
      }
    };

    database.collections.users.updateOne(query, update)
    .then(mongoUtilService.checkIfUpdateOneWasSuccessful)
    .then(userResult => {
      responseData.success = true;
      resolve(responseData);
    })
    .catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'logout';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.message = 'uncaught error';
        errorCode = ERROR.UNCAUGHT_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

module.exports.register = function(username, password, email, role, imageUrl) {
  return new Promise((resolve, reject) => {
    const userToRegister = {
      'expiryDate': tokenService.getNewExpiryDate(validTimeOfTokenInMs),
      'password': password,
      'username': username,
      'email': email,
      'role': role,
      'userId': uuidService.generateUUID(),
      'authType': AUTH_TYPE.PASSWORD,
      'imageUrl': imageUrl
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
          'accessToken': tokenService.generateAccessToken(toEncode, config.jwtSimpleSecret),
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
  let options = {fields: {username: true, groupIds: true, email: true, userId: true, role: true, imageUrl: true}};
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
        authType: 1,
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

module.exports.updateUser = function(userId, userData) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    checkIfUserIdIsGiven(userId).then(() => {
      const query = {'userId': userId};
      const update = {
        '$set': userData
      };
      const options = {upsert: false};
      return database.collections.users.updateOne(query, update, options);
    })
    .then(mongoUtilService.checkIfUpdateOneWasSuccessful)
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
