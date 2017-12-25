const winston = require('winston');
const httpResponseService = require('../services/httpResponse.service');

exports.getStatus = function(req, res) {
    const responseData = {
      'success': true,
      'payload': {
        'status': 'healthy'
      }
    };
    httpResponseService.send(res, 200, responseData);
  };
