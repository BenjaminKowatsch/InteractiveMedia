'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');

const ExpectError = function() {
  this.statusCode = null;
  this.dataPath = null;
  this.message = null;
  return this;
};

ExpectError.prototype.withStatusCode = function(statusCode) {
  this.statusCode = statusCode;
  return this;
};

ExpectError.prototype.withDataPath = function(dataPath) {
  this.dataPath = dataPath;
  return this;
};

ExpectError.prototype.withMessage = function(message) {
  this.message = message;
  return this;
};

ExpectError.prototype.inResponse = function(res) {
  expect(res).to.have.status(this.statusCode);
  expect(res).to.be.json;
  expect(res.body).to.be.an('object');
  expect(res.body.success).to.be.false;
  expect(res.body.payload).to.be.an('object');
  expect(res.body.payload.dataPath).to.equal(this.dataPath);
  expect(res.body.payload.message).to.equal(this.message);
};

module.exports = ExpectError;
