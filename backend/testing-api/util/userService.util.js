'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

module.exports.register = function(data) {
  return chai.request(settings.host).post(settings.url.users.base).send(data);
};

module.exports.loginPassword = function(data) {
  return chai.request(settings.host).post(settings.url.users.base + '/login?type=0').send(data);
};

module.exports.loginFacebook = function(data) {
  return chai.request(settings.host).post(settings.url.users.base + '/login?type=2').send(data);
};
