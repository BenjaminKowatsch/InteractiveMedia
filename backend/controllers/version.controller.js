var appVersionService = require('../services/appVersion.service');

exports.getVersion = function(req, res) {
  currentAppVersion = appVersionService.getAppVersion();
  res.status(200).json(currentAppVersion);
};
