'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');

const ExpectError = require('./expectError.util');

module.exports.toBe400 = {
  invalidRequestBody: function(res) {
    new ExpectError()
    .withStatusCode(400)
    .withMessage('invalid request body')
    .withDataPath('validation')
    .inResponse(res);
  },
  invalidAuthType: function(res) {
    new ExpectError()
    .withStatusCode(400)
    .withDataPath('authType')
    .withMessage('invalid auth type')
    .inResponse(res);
  },
  createGroup: {
    duplicatedUsers: function(res) {
      new ExpectError()
      .withStatusCode(400)
      .withDataPath('groupUsers')
      .withMessage('Duplicated groupUsers')
      .inResponse(res);
    },
    missingCreator: function(res) {
      new ExpectError()
      .withStatusCode(400)
      .withDataPath('groupUsers')
      .withMessage('GroupCreator must be part of groupUsers')
      .inResponse(res);
    },
  },
};

module.exports.toBe401 = {
  invalidAuthToken: function(res) {
    new ExpectError()
    .withStatusCode(401)
    .withDataPath('authentication')
    .withMessage('invalid authToken')
    .inResponse(res);
  },
  loginFailed: function(res) {
    new ExpectError()
    .withStatusCode(401)
    .withDataPath('login')
    .withMessage('login failed')
    .inResponse(res);
  },
  missingHeaderAuthorization: function(res) {
    new ExpectError()
    .withStatusCode(401)
    .withDataPath('authentication')
    .withMessage('missing http request header Authorization')
    .inResponse(res);
  },
  invalidFormatHeaderAuthorization: function(res) {
    new ExpectError()
    .withStatusCode(401)
    .withDataPath('authentication')
    .withMessage('invalid format of http request header Authorization')
    .inResponse(res);
  },
  unknownUserOrExpiredToken: function(res) {
    new ExpectError()
    .withStatusCode(401)
    .withDataPath('user')
    .withMessage('unknown user or expired token')
    .inResponse(res);
  },
};

module.exports.toBe403 = {
  groups: {
    userIsNotMember: function(res) {
      new ExpectError()
      .withStatusCode(403)
      .withDataPath('authorization')
      .withMessage('user is not but has to be a member of the group')
      .inResponse(res);
    },
  },
  unauthorized: function(res) {
    new ExpectError()
    .withStatusCode(403)
    .withDataPath('authorization')
    .withMessage('user is not authorized')
    .inResponse(res);
  },
};

module.exports.toBe404 = {
  groupNotFound: function(res) {
    new ExpectError()
    .withStatusCode(404)
    .withDataPath('group')
    .withMessage('group not found')
    .inResponse(res);
  },
  userNotFound: function(res) {
    new ExpectError()
    .withStatusCode(404)
    .withDataPath('user')
    .withMessage('user not found')
    .inResponse(res);
  },
};

module.exports.toBe409 = {
  createGroup: {
    nonExistingUser: function(res, emailOfNotExistingUser) {
      new ExpectError()
      .withStatusCode(409)
      .withDataPath('groupUsers')
      .withMessage('Unknown user: ' + emailOfNotExistingUser)
      .inResponse(res);
    },
  }
};
