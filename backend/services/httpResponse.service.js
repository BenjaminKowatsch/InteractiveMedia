'use strict';

var winston = require('winston');

exports.send = function(res, statusCode, body) {
    winston.debug('Sending http response:' + statusCode, body);
    res.status(statusCode).jsonp(body);
  };
