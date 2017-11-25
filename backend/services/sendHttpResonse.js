var winston = require('winston');

exports.sendHttpResponse = function(res, statusCode, body) {
    winston.info('Sending http response:' + statusCode, body);
    res.status(statusCode).jsonp(body);
  };

