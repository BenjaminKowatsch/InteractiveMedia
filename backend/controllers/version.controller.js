'use strict';

const appVersionService = require('../services/appVersion.service');
const httpResponseService = require('../services/httpResponse.service');

module.exports.getVersion = function(req, res) {
  const currentAppVersion = appVersionService.getAppVersion();
  const responseData = {
    'success': true,
    'payload': currentAppVersion
  };
  httpResponseService.send(res, 200, responseData);
};
