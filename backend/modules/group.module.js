'use strict';
const group = module.exports = {};

const winston = require('winston');
const config = require('./config');
const uuidService = require('../services/uuid.service');
const database = require('../modules/database.module');
const tokenService = require('../services/token.service');
const ERROR = require('../config.error');

/**
 * @param  {String} authToken auth token of user
 * @param  {String} groupData.name name of new group
 * @param  {String} groupData.imageUrl url of group image, can be null
 * @param  {Array<String>} groupData.users array of user emails for group
 * @return {Promise}
 *    then: {Object}
 *      {Boolean} success Flag to indicate the successful request
 *      {Object} groupData Object which was inserted
 *    catch: {Object}
 *      {String} errorCode Kind of error which occured
 *      {Object} responseData Object with error details
 **/
group.createNewGroup = function(creatorId, groupData) {
  return new Promise((resolve, reject) => {
    winston.debug('Hello from module createNewGroup');
    let responseData = {payload: {}};
    groupData.groupId = uuidService.generateUUID();
    groupData.createdAt = new Date();
    groupData.transactions = [];
    let errorToReturn = {isSelfProvided: true};

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
      errorToReturn.errorCode = ERROR.INVALID_CREATE_GROUP_VALUES;

      // checken if an id is null => unknonw user
      if (groupUserIds.includes(null)) {
        //filter returns true, if email of groupData.users is not in groupUserEmails
        let diffEmails = groupData.users.filter(i => groupUserEmails.indexOf(i) < 0).toString();
        errorToReturn.message = 'Unknown user: ' + diffEmails;
        errorToReturn.errorCode = ERROR.UNKNOWN_USER;
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
      let userUpdatePromises = [];
      let update = {
        '$push': {
          'groupIds': groupData.groupId
        }
      };
      for (let i = 0; i < groupData.users.length; i++) {
        userUpdatePromises.push(database.collections.users.findOne());
        database.collections.users.updateOne({userId: groupData.users[i]}, update);
      }
      return Promise.all(userUpdatePromises);
    }).then(result => {
      winston.debug('Creating a new group successful');
      responseData.payload = groupData;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      let errorCode;
      winston.error('Creating a new group failed');
      winston.error(err);
      responseData.success = false;
      if (err.isSelfProvided) {
        responseData.payload.dataPath = err.dataPath;
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
      } else {
        responseData.payload.dataPath = 'group';
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
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

