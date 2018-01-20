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
