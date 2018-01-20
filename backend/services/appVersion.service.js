'use strict';

module.exports.getAppVersion = function() {
  return {
      'name': process.env.npm_package_name,
      'version': process.env.npm_package_version
    };
};
