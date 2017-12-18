const winston = require('winston');
const httpResonseService = require('../services/httpResonse.service');

module.exports.getAuthenticationNotRequired = function(req, res) {
  httpResonseService.sendHttpResponse(res, 200, {'success': true, 'payload': {}});
};

module.exports.getAuthenticationRequired = function(req, res) {
  httpResonseService.sendHttpResponse(res, 200, {'success': true, 'payload': {}});
};
