const winston = require('winston');
const httpResonseService = require('../services/httpResonse.service');

exports.getStatus = function(req, res) {
    const responseData = {
      'success': true,
      'payload': {
        'status': 'healthy'
      }
    };
    httpResonseService.sendHttpResponse(res, 200, responseData);
  };
