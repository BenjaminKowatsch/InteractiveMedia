'use strict';
const group = module.exports = {};

const winston = require('winston');
/* Application configuration */
const config = require('./config');
const uuidService = require('../services/uuid.service');
const database = require('../modules/database');

var MONGO_DB_CONNECTION_ERROR_CODE = 10;
var MONGO_DB_REQUEST_ERROR_CODE = 9;

var MONGO_DB_CONNECTION_ERROR_OBJECT = {'errorCode': MONGO_DB_CONNECTION_ERROR_CODE};
/**
 * [description]
 *
 * @param  {JSONObject} groupData   group to be inserted into the database
 *                                  {String} 'name'
 *                                  {String} 'imageUrl'
 *                                  {JSONArray} 'users' consisting of userEmail Strings
 * @return {Promise}                 [description]
 */
group.createNewGroup = function(groupData) {
  winston.info('Hello from createNewGroup');
  return new Promise((resolve, reject) => {
    var responseData = {};
    groupData.groupId = uuidService.generateUUID();
    groupData.createdAt = new Date();

    responseData.payload = {};
    database.collections.groups.insertOne(groupData).then(result => {
      responseData.payload = groupData;
      delete responseData.payload._id;
      responseData.success = true;
      winston.debug('Creating a new group successful');
      resolve(responseData);
    }).catch(err => {
      responseData.payload.message = 'Error: ' + err;
      responseData.success = false;
      winston.debug('Creating a new group failed ');
      reject(responseData);
    });
  });
};

group.verifyGroupContainsUser = function(userId, groupId) {
  return new Promise((resolve, reject) => {
    let query = {objectId: groupId};
    let options = {fields: {objectId: true, users: true}};
    database.collections.groups.findOne(query, options, function(error, result) {
      if (error === null && result !== null) {
        let promiseData = {
          groupId: result.objectId,
          users: result.users,
        };
        if (result.users.indexOf(userId) > 0) {
          resolve(promiseData);
        } else {
          reject(promiseData);
        }
      } else {
        winston.error('Error MONGO_DB_INTERNAL_ERROR: ', error);
        reject(error);
      }
    });
  });
};

