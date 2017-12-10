var winston = require('winston');

exports.sendHttpResponse = function(res, statusCode, body) {
    winston.debug('Sending http response:' + statusCode, body);
    res.status(statusCode).jsonp(body);
  };

