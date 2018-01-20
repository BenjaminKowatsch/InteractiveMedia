'use strict';

const winston = require('winston');
const ERROR = require('../config/error.config');

module.exports.checkIfFindOneAndUpdateWasSuccessful = function(result) {
  return new Promise((resolve, reject) => {
    if (result && result.value && result.lastErrorObject.n === 1 &&
        result.lastErrorObject.updatedExisting && result.ok === 1) {
      resolve(result);
    } else if (result && result.lastErrorObject.n === 0 &&
      !result.lastErrorObject.updatedExisting && result.ok === 1) {
      // no error during update but nothing modified because document was not found
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'resource not found';
      errorToReturn.errorCode = ERROR.RESOURCE_NOT_FOUND;
      reject(errorToReturn);
    } else {
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'database error';
      errorToReturn.errorCode = ERROR.DB_ERROR;
      reject(errorToReturn);
    }
  });
};

module.exports.checkIfUpdateOneWasSuccessful = function(resultRaw) {
  return new Promise((resolve, reject) => {
    const result = JSON.parse(resultRaw);
    winston.debug('checkIfUpdateOneWasSuccessful:result', JSON.stringify(result));
    if (result && result.n === 1 && result.nModified === 1 && result.ok === 1) {
      resolve(result);
    } else if (result && result.n === 0 && result.nModified === 0 && result.ok === 1) {
      // no error during update but nothing modified because document was not found
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'resource not found';
      errorToReturn.errorCode = ERROR.RESOURCE_NOT_FOUND;
      reject(errorToReturn);
    } else {
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'database error';
      errorToReturn.errorCode = ERROR.DB_ERROR;
      reject(errorToReturn);
    }
  });
};

module.exports.checkIfUpdateOneUpsertWasSuccessful = function(resultRaw) {
  return new Promise((resolve, reject) => {
    const result = JSON.parse(resultRaw);

    // no error
    const success = result.ok === 1;

    // doc was found and updated
    const docUpdated = result.n === 1 && result.nModified === 1;

    // doc was found and not updated because doc to update is the same as the existing doc in collection
    const docNotUpdated = result.n === 1 && result.nModified === 0;

    // doc was not found and hence upserted
    const docUpserted = docNotUpdated && result.upserted && result.upserted.length == 1 && result.upserted[0]._id;

    if (success && (docUpdated || docNotUpdated || docUpserted)) {
      resolve(result);
    } else {
      let errorToReturn = {isSelfProvided: true};
      errorToReturn.message = 'database error';
      errorToReturn.errorCode = ERROR.DB_ERROR;
      reject(errorToReturn);
    }
  });
};
