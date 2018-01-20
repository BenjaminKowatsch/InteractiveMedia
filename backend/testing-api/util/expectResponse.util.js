'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');

const ExpectError = require('./expectError.util');

module.exports.toBe400InvalidRequestBody = function(res) {
  new ExpectError()
  .withStatusCode(400)
  .withMessage('invalid request body')
  .withDataPath('validation')
  .inResponse(res);
};

module.exports.toBe401InvalidAuthToken = function(res) {
  new ExpectError()
  .withStatusCode(401)
  .withDataPath('authentication')
  .withMessage('invalid authToken')
  .inResponse(res);
};

module.exports.toBe401LoginFailed = function(res) {
  new ExpectError()
  .withStatusCode(401)
  .withDataPath('login')
  .withMessage('login failed')
  .inResponse(res);
};

module.exports.toBe400InvalidAuthType = function(res) {
  new ExpectError()
  .withStatusCode(400)
  .withDataPath('authType')
  .withMessage('invalid auth type')
  .inResponse(res);
};

module.exports.toBe401MissingHeaderAuthorization = function(res) {
  new ExpectError()
  .withStatusCode(401)
  .withDataPath('authentication')
  .withMessage('missing http request header Authorization')
  .inResponse(res);
};

module.exports.toBe400CreateGroupDuplicatedUsers = function(res) {
  new ExpectError()
  .withStatusCode(400)
  .withDataPath('groupUsers')
  .withMessage('Duplicated groupUsers')
  .inResponse(res);
};

module.exports.toBe400CreateGroupMissingCreator = function(res) {
  new ExpectError()
  .withStatusCode(400)
  .withDataPath('groupUsers')
  .withMessage('GroupCreator must be part of groupUsers')
  .inResponse(res);
};

module.exports.toBe409CreateGroupNonExistingUser = function(res, emailOfNotExistingUser) {
  new ExpectError()
  .withStatusCode(409)
  .withDataPath('groupUsers')
  .withMessage('Unknown user: ' + emailOfNotExistingUser)
  .inResponse(res);
};

module.exports.toBe401InvalidFormatHeaderAuthorization = function(res) {
  new ExpectError()
  .withStatusCode(401)
  .withDataPath('authentication')
  .withMessage('invalid format of http request header Authorization')
  .inResponse(res);
};

module.exports.toBe403GroupsUserIsNotMember = function(res) {
  new ExpectError()
  .withStatusCode(403)
  .withDataPath('authorization')
  .withMessage('user is not but has to be a member of the group')
  .inResponse(res);
};

module.exports.toBe404GroupNotFound = function(res) {
  new ExpectError()
  .withStatusCode(404)
  .withDataPath('group')
  .withMessage('group not found')
  .inResponse(res);
};

module.exports.toBe403Unauthorized = function(res) {
  new ExpectError()
  .withStatusCode(403)
  .withDataPath('authorization')
  .withMessage('user is not authorized')
  .inResponse(res);
};

module.exports.toBe404UserNotFound = function(res) {
  new ExpectError()
  .withStatusCode(404)
  .withDataPath('user')
  .withMessage('user not found')
  .inResponse(res);
};

module.exports.toBe401UnknownUserOrExpiredToken = function(res) {
  new ExpectError()
  .withStatusCode(401)
  .withDataPath('user')
  .withMessage('unknown user or expired token')
  .inResponse(res);
};
