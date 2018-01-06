'use strict';

const appVersionService = require('../services/appVersion.service');
const httpResponseService = require('../services/httpResponse.service');

exports.getVersion = function(req, res) {
  const currentAppVersion = appVersionService.getAppVersion();
  httpResponseService.send(res, 200, currentAppVersion);
};
