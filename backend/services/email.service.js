'use strict';

module.exports.generateFacebookEmailFromUsername = function(userId, username) {
    username = username.toLowerCase();
    username = username.replace(/\s/g, '_');
    username = username + '.' + parseInt(userId) % 100 + '@fb.com';
    return username;
  };
