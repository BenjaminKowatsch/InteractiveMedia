const express = require('express');

const database = require('../modules/database');
const group = require('../modules/group');
const winston = require('winston');
const httpResonseService = require('./httpResonse.service');

exports.isAuthorizedAdmin = function(req, res, next) {
  winston.info('Authorizing request as admin');
  let admin = 'admin';
  if (isAuthenticated(req, res)) {
    let userId = res.locals.userId;
    verifyRole(userId, admin).
        then(promise => next()).
        catch((error) => {
          winston.error('Non-admin user attempted to access /groups: ' +
              userId);
          const resBody = {'success': false, 'payload': error.message};
          httpResonseService.sendHttpResponse(res, 403, resBody);
        });
  } else {
    let error = 'Authorization failed due to missing authentication';
    let resBody = {'success': false, 'payload': error};
    httpResonseService.sendHttpResponse(res, 500, resBody);
  }
};

exports.isGroupMember = function(req, res, next) {
  winston.info('Authorizing request as user');
  if (isAuthenticated(req, res)) {
    let userId = res.locals.userId;
    if (req.path.includes('groups')) {
      let groupId = req.params.groupid;
      verifyUserInGroup(userId, groupId).then((promiseData) => {
        res.locals.groupId = promiseData.groupId;
        next();
      }).catch((error) => {
        winston.error('User ' + userId + ' could not be authorized for group ' +
            groupId);
        let resBody = {'success': false, 'payload': error.message};
        httpResonseService.sendHttpResponse(res, 403, resBody);
      });
    } else {
      next();
    }
  } else {
    let error = 'Authorization failed due to missing authentication';
    let resBody = {'success': false, 'payload': error};
    httpResonseService.sendHttpResponse(res, 500, resBody);
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

function verifyUserInGroup(userId, groupId) {
  return group.verifyGroupContainsUser(database.collections.groups, groupId,
      userId);
}
