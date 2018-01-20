'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const https = require('https');
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

module.exports.getUserData = function(authType, authToken) {
  return chai.request(settings.host)
  .get(settings.url.users.base  + '/user')
  .set('Authorization', authType + ' ' + authToken);
};

module.exports.getFacebookTestAccessToken = function() {
  return new Promise((resolve, reject) => {
    https.get('https://graph.facebook.com/v2.11/' + settings.facebook.appId + '/' +
    'accounts/test-users?access_token=' + settings.facebook.urlAppToken, function(response) {
      let responseMessage = '';

      response.on('data', function(chunk) {
        responseMessage += chunk;
      });

      response.on('end', function() {
        const data = JSON.parse(responseMessage);
        if (data.length <= 0) {
          reject(data);
        } else {
          resolve(data.data[0].access_token);
        }
      });
    });
  });
};
