'use strict';

const winston = require('winston');
const httpResponseService = require('../services/httpResponse.service');

module.exports.getStatus = function(req, res) {
  const responseData = {
    'success': true,
    'payload': {
      'status': 'healthy'
    }
  };
  httpResponseService.send(res, 200, responseData);
};
