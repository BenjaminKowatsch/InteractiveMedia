'use strict';

const winston = require('winston');
const config = require('../config/settings.config');
const uuidService = require('../services/uuid.service');
const database = require('../modules/database.module');
const tokenService = require('../services/token.service');
const ERROR = require('../config/error.config');
const SPLIT = require('../config/split.config');

module.exports.createNewGroup = function(creatorId, groupData) {
  return new Promise((resolve, reject) => {
    winston.debug('Hello from module createNewGroup');
    let responseData = {payload: {}};
    groupData.groupId = uuidService.generateUUID();
    groupData.createdAt = new Date();
    groupData.transactions = [];
    let groupUserObjects;

    findUsersByField('email', groupData.users)
    .then(findUsersResult => checkForInvalidCreateGroupValues(findUsersResult, groupData.users, creatorId))
    .then(groupUser => {
      groupData.users = groupUser.ids;
      groupUserObjects = groupUser.objects;
      return database.collections.groups.insertOne(groupData);
    }).then(() => addGroupIdToUsers(groupData.groupId, groupData.users))
    .then(result => {
      winston.debug('Creating a new group successful');
      groupData.users = groupUserObjects; //returns also the username
      responseData.payload = groupData;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      let errorCode;
      winston.debug('Creating a new group failed');
      winston.debug(err);
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

module.exports.getGroupById = function(groupId) {
  winston.debug('Hello from module getGroupById');
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    checkIfGroupIdIsGiven(groupId)
    .then(findGroupById)
    .then(checkForGroupResult)
    .then(groupResult => {
      responseData.payload = groupResult;
      return findUsersByField('userId', groupResult.users);
    }).then(checkUserResults)
    .then(userResults => {
      let groupUserObjects = userResults.map(val => ({
        userId: val.userId,
        username: val.username,
        email: val.email,
        imageUrl: val.imageUrl
      }));
      responseData.payload.users = groupUserObjects;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'group';
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

module.exports.verifyGroupContainsUser = function(userId, groupId) {
  return new Promise((resolve, reject) => {
    let errorToReturn = {isSelfProvided: true};
    checkIfGroupIdIsGiven(groupId)
    .then(findGroupById)
    .then(checkForGroupResult)
    .then(groupResult => checkIfUserIdIsInGroup(groupResult, userId))
    .then(groupResult => {
      resolve(groupResult);
    }).catch(err => {
      winston.debug(err);
      let responseData = {payload: {dataPath: 'authorization'}, success: false};
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.dataPath = err.dataPath;
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

module.exports.getAllGroups = function() {
  winston.debug('getAllGroups');
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    const aggregation = {
      $project: {
        _id: 0,
        imageUrl: 1,
        groupId: 1,
        createdAt: 1,
        name: 1,
        countUsers: {$size: '$users'},
        countTransactions: {$size: '$transactions'}
      }
    };
    aggregateGroups(aggregation).then(result => {
      responseData.payload.groups = result;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'group';
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

function validateSplit(group, transaction) {
  let errorToReturn = {isSelfProvided: true};
  errorToReturn.dataPath = 'transaction';
  errorToReturn.message = 'invalid split';
  let splitInvalid = false;

  // validate split types individually
  transaction.split.forEach((split, index) => {
    switch (split.type) {
      case SPLIT.EVEN:
        // SPLIT.EVEN hast to be last in the array
        if (transaction.split.length - 1 != index) {
          splitInvalid = true;
          errorToReturn.errorCode = ERROR.INVALID_SPLIT;
          break;
        }
        break;
      case SPLIT.CONSTANT_DEDUCTION:
        // userId has to be member of the group
        if (group.users.indexOf(split.userId) < 0) {
          errorToReturn.dataPath = 'authorization';
          errorToReturn.message = 'user is not but has to be a member of the group';
          splitInvalid = true;
          errorToReturn.errorCode = ERROR.USER_NOT_IN_GROUP;
          break;
        }
        break;
    }
  });
  if (splitInvalid) {
    return Promise.reject(errorToReturn);
  } else {
    return group;
  }
}

module.exports.createNewTransaction = function(groupId, transactionData) {
  return new Promise((resolve, reject) => {
    winston.debug('Hello from module createNewTransaction');
    let responseData = {payload: {}};
    transactionData.publishedAt = new Date();

    checkIfGroupIdIsGiven(groupId)
    .then(findGroupById)
    .then(checkForGroupResult)
    .then(groupResult => checkIfUserIdIsInGroup(groupResult, transactionData.paidBy))
    .then(groupResult => checkIfDateIsGtGroupCreateDate(groupResult, transactionData.infoCreatedAt))
    .then(groupResult => validateSplit(groupResult, transactionData))
    .then(groupResult => addTransactionToGroup(groupId, transactionData))
    .then(transactionResult => {
      winston.debug('Creating a new transaction successful');
      responseData.payload = transactionData;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      let errorCode;
      winston.debug('Creating a new transaction failed');
      winston.debug(err);
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

module.exports.getTransactionAfterDate = function(groupId, date) {
  winston.debug('Hello from module getTransactionAfterDate');
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};
    checkIfDateIsGivenAndValid(date)
    .then(() => checkIfGroupIdIsGiven(groupId))
    .then(findGroupById)
    .then(checkForGroupResult)
    .then(groupResult => filterTransactionsAfterDate(groupResult.transactions, date))
    .then(sortTransactionsByPublishedAt)
    .then(transactionsResult => {
      responseData.payload = transactionsResult;
      responseData.success = true;
      resolve(responseData);
    }).catch(err => {
      winston.debug(err);
      responseData.success = false;
      responseData.payload.dataPath = 'group';
      let errorCode;
      if (err.isSelfProvided) {
        responseData.payload.message = err.message;
        errorCode = err.errorCode;
        responseData.payload.dataPath = err.dataPath || responseData.payload.dataPath;
      } else {
        responseData.payload.message = 'unknown database error';
        errorCode = ERROR.DB_ERROR;
      }
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

function aggregateGroups(query) {
  return database.collections.groups.aggregate([query]).toArray();
}

function checkForInvalidCreateGroupValues(findUsersResult, requestedUserEmails, creatorId) {
  let errorToReturn = {isSelfProvided: true};
  errorToReturn.dataPath = 'groupUsers';
  errorToReturn.errorCode = ERROR.INVALID_CREATE_GROUP_VALUES;
  // return all groupUserIds if object is not null
  let groupUserIds = findUsersResult.map(val => val ? val.userId : null);
  let groupUserEmails = findUsersResult.map(val => val ? val.email : null);
  let groupUserObjects = findUsersResult.map(val => val ? {
    userId: val.userId,
    username: val.username,
    email: val.email,
    imageUrl: val.imageUrl
  } : null);
  // checken if an id is null => unknonw user
  if (groupUserIds.includes(null)) {
    //filter returns true, if email of groupData.users is not in groupUserEmails
    let diffEmails = requestedUserEmails.filter(i => groupUserEmails.indexOf(i) < 0).toString();
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
    return {objects: groupUserObjects, ids: groupUserIds};
  }
}

function findUsersByField(field, users) {
  // generate array of promises of db calls for find user by id
  let groupUserPromises = [];
  for (let i = 0; i < users.length; i++) {
    groupUserPromises.push(database.collections.users.findOne({[field]: users[i]}));
  }
  return Promise.all(groupUserPromises);
}

function addGroupIdToUsers(groupId, userIds) {
  let userUpdatePromises = [];
  let update = {
    '$push': {
      'groupIds': groupId
    }
  };
  for (let i = 0; i < userIds.length; i++) {
    userUpdatePromises.push(database.collections.users.updateOne({userId: userIds[i]}, update));
  }
  return Promise.all(userUpdatePromises);
}

function findGroupById(groupId) {
  let query = {groupId: groupId};
  let options = {fields: {_id: false}};
  return database.collections.groups.findOne(query, options);
}

function checkIfGroupIdIsGiven(groupId) {
  if (!groupId) {
    let errorToReturn = {isSelfProvided: true};
    errorToReturn.dataPath = 'group';
    errorToReturn.message = 'missing groupId in URL';
    errorToReturn.errorCode = ERROR.MISSING_ID_IN_URL;
    return Promise.reject(errorToReturn);
  } else {
    return Promise.resolve(groupId); // must resolve -> groupId is not an object
  }
}

function checkForGroupResult(groupResult) {
  if (!groupResult) {
    let errorToReturn = {isSelfProvided: true};
    errorToReturn.dataPath = 'group';
    errorToReturn.message = 'group not found';
    errorToReturn.errorCode = ERROR.UNKNOWN_GROUP;
    return Promise.reject(errorToReturn);
  } else {
    return groupResult;
  }
}

function checkUserResults(userResults) {
  if (userResults.includes(null)) {
    let errorToReturn = {isSelfProvided: true};
    errorToReturn.message = 'Unknown user in group';
    errorToReturn.errorCode = ERROR.UNKNOWN_USER;
    return Promise.reject(errorToReturn);
  } else {
    return userResults;
  }
}

function checkIfUserIdIsInGroup(groupResult, userId) {
  if (groupResult.users.indexOf(userId) < 0) {
    let errorToReturn = {isSelfProvided: true};
    errorToReturn.dataPath = 'authorization';
    errorToReturn.message = 'user is not but has to be a member of the group';
    errorToReturn.errorCode = ERROR.USER_NOT_IN_GROUP;
    return Promise.reject(errorToReturn);
  } else {
    return groupResult;
  }
}

function addTransactionToGroup(groupId, transaction) {
  const update = {
    '$push': {
      'transactions': transaction
    }
  };
  return database.collections.groups.updateOne({groupId: groupId}, update);
}

function checkIfDateIsGtGroupCreateDate(groupResult, transactionDate) {
  if (new Date(groupResult.createdAt) > new Date(transactionDate)) {
    let errorToReturn = {isSelfProvided: true};
    errorToReturn.dataPath = 'transaction';
    errorToReturn.message = 'invalid time adjustment: group.createdAt is gt transaction.infoCreatedAt';
    errorToReturn.errorCode = ERROR.INVALID_CREATE_TRANSACTION_VALUES;
    return Promise.reject(errorToReturn);
  } else {
    return groupResult;
  }
}

function filterTransactionsAfterDate(allTransactions, date) {
  const td = dateSting => new Date(dateSting);
  if (allTransactions.length) {
    let transactions = allTransactions.filter((val, i, self) => td(val.publishedAt) > td(date));
    return transactions;
  } else {
    return [];
  }
}

function sortTransactionsByPublishedAt(transactions) {
  const td = dateSting => new Date(dateSting);
  return transactions  // sort ascending by publishedAt
  .sort((a,b) => td(a.publishedAt) > td(b.publishedAt) ? 1 : td(a.publishedAt) < td(b.publishedAt) ? -1 : 0);
}

function checkIfDateIsGivenAndValid(date) {
  //matches exactly ISO-8601, format: YYYY-MM-DDTHH:mm:ss.sssZ
  let dateRegExp = /^\d{4}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d\.\d{3}Z$/;
  let errorToReturn = {isSelfProvided: true};
  errorToReturn.dataPath = 'urlParamAfter';
  if (!date) {
    errorToReturn.message = 'missing param after in URL';
    errorToReturn.errorCode = ERROR.MISSING_ID_IN_URL;
    return Promise.reject(errorToReturn);
  } else if (!dateRegExp.test(date)) {
    errorToReturn.message = 'invalid date format';
    errorToReturn.errorCode = ERROR.INVALID_DATE_FORMAT;
    return Promise.reject(errorToReturn);
  } else {
    return Promise.resolve(date); // must resolve -> date is not an object
  }
}
