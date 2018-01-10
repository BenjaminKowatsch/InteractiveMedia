'use strict';

module.exports.generateFacebookEmailFromUsername = function(userId, username) {
    username = username.toLowerCase();
    username = username.replace(/ /g, '_');
    username = username + '.' + userId + '@facebook.debtsquared.com';
    return username;
  };
