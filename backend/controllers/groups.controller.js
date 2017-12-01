const winston = require('winston');

exports.getAll = function(req, res) {
  winston.info('Getting all groups');
  res.status(404);
};

exports.getById = function(req, res) {
  const groupId = req.params.groupid;
  winston.info('Getting group with id' + groupId);
  res.status(404);
};
