'use strict';

const winston = require('winston');

module.exports.send = function(res, statusCode, body) {
  winston.debug('Sending http response:' + statusCode, body);
  res.status(statusCode).jsonp(body);
};
