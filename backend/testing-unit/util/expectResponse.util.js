'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');

const ERROR = require('../../config/error.config');

module.exports.error = {
  invalidRequestBody: {
    errorCode: ERROR.INVALID_REQUEST_BODY,
    responseData: {
      success: false,
      payload: {
        dataPath: 'validation',
        message: 'invalid request body'
      }
    }
  },
};

module.exports.success = {
  emptyPayload: {
    success: true,
    payload: {}
  },
};
