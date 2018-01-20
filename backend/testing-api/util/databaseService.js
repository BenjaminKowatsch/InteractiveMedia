'use strict';

const MongoClient = require('mongodb').MongoClient;
const winston = require('winston');
const settings = require('../config/settings.config');
const adminData = require('../test/data/admin.data');

let database = {};
database.collections = {};

function waitOrOrderForDB() {
  return new Promise((resolve,reject) => {
    if (database.db) {
      resolve();
    } else {
      MongoClient.connect(settings.mongoDb.connect.url, settings.mongoDb.connect.options).then(db => {
        console.log('DB connection established');
        database.db = db;
        resolve();
      }).catch(err => {
        winston.error('Database connection failed with error: ' + err);
        database.collections.users = undefined;
        database.collections.groups = undefined;
        database.db = undefined;
        reject();
      });
    }
  });
}

let mutex = Promise.resolve();
let mutexResolver;
function waitForAndSetMutex() {
  return mutex.then(() => {
    mutex = new Promise((resolve)=> (mutexResolver = resolve));
    return true;
  });
}

function promiseResetDB() {
  return new Promise((resolve,reject) => {
    let db;
    waitForAndSetMutex()
    .then(waitOrOrderForDB)
    .then(() => {
      db = database.db;
      winston.debug('Database connection established');
      return db.dropCollection('users');
    }).then(result => {
      return db.dropCollection('groups');
    }).then(result => {
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
          winston.debug('create user: admin');
          const adminUser = {
            'expiryDate': new Date(new Date().getTime() + 3600000),
            'password': adminData.password,
            'username': adminData.username,
            'email': adminData.email,
            'role': adminData.role,
            'userId': adminData.userId,
            'authType': adminData.authType,
            'imageUrl': null
          };
          return database.collections.users.insertOne(adminUser);
        })
        .then(() => {
          winston.debug('Database cleaned successfully');
          mutexResolver();
          resolve();
        }).catch(error => {
          winston.error('Error while resetting database', JSON.stringify(error));
          reject();
        });
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
