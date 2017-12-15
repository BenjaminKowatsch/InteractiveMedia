'use strict';
const group = module.exports = {};

const winston = require('winston');
const config = require('./config');
const uuidService = require('../services/uuid.service');
const database = require('../modules/database');
const tokenService = require('../services/token.service');

/**
 * [description]
 *
 * @param  {JSONObject} groupData   group to be inserted into the database
 *                                  {String} 'name'
 *                                  {String} 'imageUrl'
 *                                  {JSONArray} 'users' consisting of userEmail Strings
 * @return {Promise}                 [description]
 */
group.createNewGroup = function(requestBody) {
  return new Promise((resolve, reject) => {
    winston.debug('Hello from module createNewGroup');
    let responseData = {payload: {}};
    let groupData = requestBody.payload;
    groupData.groupId = uuidService.generateUUID();
    groupData.createdAt = new Date();
    groupData.transactions = [];
    let errorToReturn = {isSelfProvided: true};
    let creatorId = tokenService.decodeToken(requestBody.accessToken).userId;

    // generate array of promises of db calls for find user by id
    let groupUserPromises = [];
    for (let i = 0; i < groupData.users.length; i++) {
      groupUserPromises.push(database.collections.users.findOne({email: groupData.users[i]}));
    }

    Promise.all(groupUserPromises).then(findOneValues => {
      // return all groupUserIds if object is not null
      let groupUserIds = findOneValues.map(val => val ? val.userId : null);
      let groupUserEmails = findOneValues.map(val => val ? val.email : null);
      errorToReturn.dataPath = 'groupUsers';

      // checken if an id is null => unknonw user
      if (groupUserIds.includes(null)) {
        //filter returns true, if email of groupData.users is not in groupUserEmails
        let diffEmails = groupData.users.filter(i => groupUserEmails.indexOf(i) < 0).toString();
        errorToReturn.message = 'Unknown user: ' + diffEmails;
        return Promise.reject(errorToReturn);
      //check if groupUsers contains groupCreator by creatorId
      } else if (!groupUserIds.includes(creatorId)) {
        errorToReturn.message = 'GroupCreator must be part of groupUsers';
        return Promise.reject(errorToReturn);
      // check for duplicated groupUsers by filter (-> has length > 0 if duplicated entries)
      } else if (groupUserIds.filter((val, i, self) => self.indexOf(val) !== i).length) {
        errorToReturn.message = 'Duplicated groupUsers';
        return Promise.reject(errorToReturn);
      // no error -> replace emails with ids and insert into db
      } else {
        groupData.users = groupUserIds;
        return database.collections.groups.insertOne(groupData);
      }
    }).then(result => {
      winston.debug('Creating a new group successful');
      responseData.payload = groupData;
      responseData.success = true;
      responseData.statusCode = 201;
      resolve(responseData);
    }).catch(err => {
      winston.error('Creating a new group failed ');
      winston.error(err);
      responseData.payload.dataPath = 'group';
      responseData.payload.message = err;
      responseData.success = false;
      responseData.statusCode = 400;
      // return custom values if it's an ErrorToReturn + values available
      if (err.isSelfProvided) {
        responseData.payload.dataPath = err.dataPath || responseData.payload.dataPath;
        responseData.payload.message = err.message || responseData.payload.message;
        responseData.statusCode = err.statusCode || responseData.statusCode;
      }
      reject(responseData);
    });
  });
};

// by maxi, not refactored yet
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

