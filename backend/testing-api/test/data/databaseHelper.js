const MongoClient = require('mongodb').MongoClient;
const winston = require('winston');

var database = {};
database.collections = {};
const mongoConnectConfig = {
  bufferMaxEntries: 0,
  autoReconnect: true
};

function createIndexCallback(err, indexname) {
  if (err === null) {
    winston.debug('Created index + ' + indexname);
  } else {
    winston.debug('Creation of index + ' + indexname + ' failed');
  }
}

function promiseResetDB() {
  return new Promise((resolve,reject) => {
    var db;
    MongoClient.connect('mongodb://mongo/debtsquared', mongoConnectConfig).then(_db => {
      db = _db;
      winston.debug('Database connection established');
      return db.dropCollection('users');
    }).then(result => {
      return db.dropCollection('groups');
    }).then(result => {
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

      winston.info('Database successfully cleaned');
      db.close();
      resolve();
    }).catch(err => {
      winston.error('Database connection failed with error: ' + err);
      database.collections.users = undefined;
      database.collections.groups = undefined;
      database.db = undefined;
      db.close();
      reject();
    });
  });
}
module.exports.promiseResetDB = promiseResetDB;

module.exports.cbResetDB = function(done) {
  promiseResetDB().then(() => {
      done();
    }).catch((error) => {
      winston.error('DB Error: ' + error);
    });
};
