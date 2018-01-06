'use strict';

const winston = require('winston');
const httpResponseService = require('../services/httpResponse.service');

module.exports.getAuthenticationNotRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'open world'}});
};

module.exports.getAuthenticationRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'authenticated world'}});
};

module.exports.getAuthorizationNotRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'open world without authorization'}});
};

module.exports.getAuthorizationAdminRequired = function(req, res) {
  httpResponseService.send(res, 200, {'success': true, 'payload': {'hello': 'authorized world as admin'}});
};
