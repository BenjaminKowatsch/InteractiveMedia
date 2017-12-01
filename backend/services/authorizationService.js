const express = require('express');

const database = require('../modules/database');
const winston = require('winston');
const httpResponder = require('./sendHttpResonse');

exports.isAuthorizedAdmin = function(req, res, next) {
  winston.info('Authorizing request as admin');
  let admin = 'admin';
  if (isAuthenticated(req, res)) {
    const userId = res.locals.userId;
    verifyRole(userId, admin).
        then(promise => next()).
        catch((error) => {
          winston.error('Non-admin user attempted to access /groups: ' +
              userId);
          const resBody = {'success': false, 'payload': error.message};
          httpResponder.sendHttpResponse(res, 403, resBody);
        });
  } else {
    let error = 'Authorization failed due to missing authentication';
    let resBody = {'success': false, 'payload': error};
    httpResponder.sendHttpResponse(res, 500, resBody);
  }
};

function isAuthenticated(req, res) {
  if (res.locals.userId === undefined) {
    let errorString = 'Request on baseUrl ' + req.baseUrl + ' with path ' +
        req.path + ' cannot be authorized without prior authentication';
    winston.error(errorString);
    return false;
  } else {
    return true;
  }
}

function verifyRole(userId, roleId) {
  if (roleId === 'admin') {
    return Promise.reject(
        {authorizedAdmin: false, message: 'Admin access required'});
  } else {
    return Promise.resolve({authorizedAdmin: true});
  }
}
