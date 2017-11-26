var appVersion = require('../services/getAppVersion');

exports.getVersion = function(req, res) {
  currentAppVersion = appVersion.getAppVersion();
  res.status(200).json(currentAppVersion);
};
