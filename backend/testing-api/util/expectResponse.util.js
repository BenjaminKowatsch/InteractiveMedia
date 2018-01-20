'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');

module.exports.toBe400InvalidRequestBody = function(res) {
  expect(res).to.have.status(400);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('validation');
  expect(res.body.payload.message).to.equal('invalid request body');
};

module.exports.toBe401InvalidAuthToken = function(res) {
  expect(res).to.have.status(401);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('authentication');
  expect(res.body.payload.message).to.equal('invalid authToken');
};

module.exports.toBe401LoginFailed = function(res) {
  expect(res).to.have.status(401);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('login');
  expect(res.body.payload.message).to.equal('login failed');
};

module.exports.toBe400InvalidAuthType = function(res) {
  expect(res).to.have.status(400);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('authType');
  expect(res.body.payload.message).to.equal('invalid auth type');
};

module.exports.toBe401MissingHeaderAuthorization = function(res) {
  expect(res).to.have.status(401);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('authentication');
  expect(res.body.payload.message).to.equal('missing http request header Authorization');
};

module.exports.toBe400CreateGroupDuplicatedUsers = function(res) {
  expect(res).to.have.status(400);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('groupUsers');
  expect(res.body.payload.message).to.equal('Duplicated groupUsers');
};

module.exports.toBe400CreateGroupMissingCreator = function(res) {
  expect(res).to.have.status(400);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('groupUsers');
  expect(res.body.payload.message).to.equal('GroupCreator must be part of groupUsers');
};

module.exports.toBe409CreateGroupNonExistingUser = function(res, emailOfNotExistingUser) {
  expect(res).to.have.status(409);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('groupUsers');
  expect(res.body.payload.message).to.equal('Unknown user: ' + emailOfNotExistingUser);
};

module.exports.toBe401InvalidFormatHeaderAuthorization = function(res) {
  expect(res).to.have.status(401);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.be.equal('authentication');
  expect(res.body.payload.message).to.be.equal('invalid format of http request header Authorization');
};

module.exports.toBe403GroupsUserIsNotMember = function(res) {
  expect(res).to.have.status(403);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('authorization');
  expect(res.body.payload.message).to.equal('user is not but has to be a member of the group');
};

module.exports.toBe404GroupNotFound = function(res) {
  expect(res).to.have.status(404);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('group');
  expect(res.body.payload.message).to.equal('group not found');
};

module.exports.toBe403Unauthorized = function(res) {
  expect(res).to.have.status(403);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.be.equal('authorization');
  expect(res.body.payload.message).to.be.equal('user is not authorized');
};

module.exports.toBe404UserNotFound = function(res) {
  expect(res).to.have.status(404);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('user');
  expect(res.body.payload.message).to.equal('user not found');
};

module.exports.toBe401UnknownUserOrExpiredToken = function(res) {
  expect(res).to.have.status(401);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal('user');
  expect(res.body.payload.message).to.equal('unknown user or expired token');
};
