'use strict';

let database = module.exports = {};

const winston = require('winston');

/* MongoDB Client */
const MongoClient = require('mongodb').MongoClient;

const mongoConnectConfig = {
  bufferMaxEntries: 0,
  autoReconnect: true
};

const tryConnectOptions = {
  maxRetries: 100,
  retryInterval: 500, // in milliseconds
  currentRetryCount: 0,
  url: '',
  resolve: undefined,
  reject: undefined
};

/**
 * Export access variablse for interaction with the database
 */
database.collections = {
  'users': undefined,
  'groups': undefined
};
database.db = undefined;

/**
 * Export the function 'tryToConnectToDatabase'
 */
database.tryConnect = function(url, resolve, reject) {
  tryConnectOptions.url = url;
  tryConnectOptions.resolve = resolve;
  tryConnectOptions.reject = reject;
  tryToConnectToDatabase();
};
/**
 * Function which asynchronously loops until either a connection to the database could be established
 * or the maximum number of retries have been reached.
 * Attention: Callbacks are uses because default JS Promises do not support timeouts
 */
function tryToConnectToDatabase() {
  tryConnectOptions.currentRetryCount += 1;
  winston.debug('Database connection try number: ' + tryConnectOptions.currentRetryCount);
  connect(function() {
    tryConnectOptions.resolve();
  }, function() {
    if (tryConnectOptions.currentRetryCount < tryConnectOptions.maxRetries) {
      setTimeout(tryToConnectToDatabase, tryConnectOptions.retryInterval);
    } else {
      tryConnectOptions.reject();
    }
  });
}

/**
 * Function to connect to mongodb, if it fails the properties at the JSON object 'collections' will be set to 'undefined'.
 * Otherwise the properties will be initialized with a reference to the corresponding collection at the database.
 * Attention: Callbacks are uses because default JS Promises do not support timeouts
 *
 * @param  {function} resolve Called when the database connection has been established
 * @param  {function} reject  Called when the database connection could not be established
 */
function connect(resolve, reject) {
  /* Connect to mongodb once to reduce the number of connection pools created by our application  */

  MongoClient.connect(tryConnectOptions.url, mongoConnectConfig, function(err, db) {
    if (!err) {
      winston.debug('Database connection established');
      // Initialize database and collection access variables
      database.db = db;
      database.collections.users = db.collection('users');
      database.collections.groups = db.collection('groups');

      Promise.resolve()
      .then(() => {
        winston.debug('create index: user: email unique');
        const keys = {email: 1};
        const options = {unique: true};
        return database.collections.users.createIndex(keys, options);
      })
      .then(() => {
        winston.debug('create index: user: username, password unique');
        const keys = {
          username: 1,
          password: 1
        };
        const options = {
          unique: true,
          partialFilterExpression: {
            username: {'$exists': true},
            password: {'$exists': true}
          }
        };
        return database.collections.users.createIndex(keys, options);
      })
      .then(() => {
        winston.debug('create index: user: userId, loginType unique');
        const keys = {
          userId: 1,
          loginType: 1
        };
        const options = {unique: true};
        return database.collections.users.createIndex(keys, options);
      })
      .then(() => {
        winston.debug('create index: group: groupId unique');
        const keys = {
          groupId: 1
        };
        const options = {unique: true};
        return database.collections.groups.createIndex(keys, options);
      })
      .then(() => {
        winston.info('Indicies created');
        resolve();
      }).catch(error => {
        winston.error('Error while creating indices', JSON.stringify(error));
        reject();
      });
    } else {
      winston.error('Database connection failed with error: ' + err);
      database.collections.users = undefined;
      database.collections.groups = undefined;
      database.db = undefined;
      reject();
    }
  });
}
