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
