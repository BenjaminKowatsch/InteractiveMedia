var appVersionService = require('../services/appVersion.service');
const httpResonseService = require('../services/httpResonse.service');

exports.getVersion = function(req, res) {
  currentAppVersion = appVersionService.getAppVersion();
  httpResonseService.sendHttpResponse(res, 200, currentAppVersion);
};
