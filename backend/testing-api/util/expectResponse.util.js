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
