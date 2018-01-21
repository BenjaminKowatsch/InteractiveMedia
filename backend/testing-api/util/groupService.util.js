'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

module.exports.create = function(authType, authToken, data) {
  return chai.request(settings.host)
    .post(settings.url.groups.base)
    .set('Authorization', authType + ' ' + authToken)
    .send(data);
};

module.exports.createTransaction = function(groupId, authType, authToken, data) {
  return chai.request(settings.host)
    .post(settings.url.groups.base  + '/' + groupId + '/transactions')
    .set('Authorization', authType + ' ' + authToken)
    .send(data);
};

module.exports.sortByInfoCreatedAt = function(a, b) {
  const td = dateSting => new Date(dateSting);
  return td(a.infoCreatedAt) > td(b.infoCreatedAt) ? 1 : td(a.infoCreatedAt) < td(b.infoCreatedAt) ? -1 : 0;
};

module.exports.expectTransaction = function(given, expected) {
  expect(given).to.be.an('object');
  expect(given.infoName).to.equal(expected.infoName);
  expect(given.amount).to.equal(expected.amount);
  expect(given.infoLocation).to.deep.equal(expected.infoLocation);
  expect(given.infoCreatedAt).to.equal(expected.infoCreatedAt);
  expect(given.infoImageUrl).to.equal(expected.infoImageUrl);
  expect(given.paidBy).to.equal(expected.paidBy);
  expect(given.publishedAt).to.be.a('string').with.lengthOf(24);
};

module.exports.expectTransactionsSorted = function(transactions) {
  const names = transactions.map(t => t.infoName);
  for (let i = 0; i < transactions.length; i++) {
    expect(names[i]).to.equal('Test transaction ' + i);
  }
};
