'use strict';

/*jshint expr: true, node:true, mocha:true*/

const winston = require('winston');

module.exports.deepCopy = function(data) {
  return JSON.parse(JSON.stringify(data));
};

module.exports.wait = function(milliSeconds) {
  return new Promise(resolve => setTimeout(resolve, milliSeconds));
};
