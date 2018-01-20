'use strict';

const jwt = require('jwt-simple');
const winston = require('winston');

const config = require('../config/settings.config');
const ERROR = require('../config/error.config');

/**
 * Function to calculate a new expiry date for tokens.
 *
 * @param {Int} validTime  Time in millisecond the token should be valid
 * @param  {Date} startDate  Optional. Use startDate instead of current date to calculate expiry date
 * @return {Date}  new epxiry date
 */
module.exports.getNewExpiryDate = function(validTime, startDate) {
    let newExpDate;
    if (startDate) {
      newExpDate = startDate.getTime() + validTime;
    } else {
      newExpDate = new Date().getTime() + validTime;
    }
    return new Date(newExpDate);
  };

module.exports.generateAccessToken = function(toEncode) {
    return jwt.encode(toEncode, config.jwtSimpleSecret);
  };

module.exports.decodeToken = function(token) {
  let responseData = {payload: {}};
  try {
    const decodeResult = jwt.decode(token, config.jwtSimpleSecret);
    responseData.success = true;
    responseData.payload.userId = decodeResult.userId;
    return Promise.resolve(responseData);
  } catch (error) {
    responseData.success = false;
    responseData.payload.dataPath = 'authentication';
    responseData.payload.message = 'invalid authToken';
    let errorCode = ERROR.INVALID_AUTH_TOKEN;
    winston.error('errorCode', errorCode);
    return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
};
