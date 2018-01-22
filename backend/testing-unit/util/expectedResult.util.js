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
  invalidAuthToken: {
    errorCode: ERROR.INVALID_AUTH_TOKEN,
    responseData: {
      success: false,
      payload: {
        dataPath: 'authentication',
        message: 'invalid authToken'
      }
    }
  },
};

module.exports.success = {
  emptyPayload: {
    success: true,
    payload: {}
  },
  appVersion: {
    name: 'Backend',
    version: '0.1.0'
  },
  withPayload: function(payload) {
    return {success: true, payload: payload};
  },
};
