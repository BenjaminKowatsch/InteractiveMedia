var database = module.exports = {};

const winston = require('winston');

/* MongoDB Client */
var MongoClient = require('mongodb').MongoClient;

var mongoConnectConfig = {
  bufferMaxEntries: 0,
  autoReconnect: true
};

var tryConnectOptions = {
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

  var createIndexCallback = function(err, indexname) {
    if (err === null) {
      winston.debug('Created index + ' + indexname);
    } else {
      winston.debug('Creation of index + ' + indexname + ' failed');
    }
  };

  MongoClient.connect(tryConnectOptions.url, mongoConnectConfig, function(err, db) {
    if (!err) {
      winston.debug('Database connection established');
      // Initialize database and collection access variables
      database.db = db;
      database.collections.users = db.collection('users');
      database.collections.groups = db.collection('groups');

      database.collections.users.createIndex({
        email: 1
      }, {
        unique: true
      }, createIndexCallback);

      database.collections.users.createIndex({
        username: 1,
        password: 1
      }, {
        unique: true,
        partialFilterExpression: {
          username: {
            '$exists': true
          },
          password: {
            '$exists': true
          }
        }
      }, createIndexCallback);

      database.collections.users.createIndex({
        userId: 1,
        loginType: 1
      }, {
        unique: true
      }, createIndexCallback);

      database.collections.groups.createIndex({
        groupId: 1
      }, {
        unique: true
      }, createIndexCallback);

      resolve();
    } else {
      winston.error('Database connection failed with error: ' + err);
      database.collections.users = undefined;
      database.collections.groups = undefined;
      database.db = undefined;
      reject();
    }
  });

}
