var group = module.exports = {};

var winston = require('winston');
/* Application configuration */
var config = require('./config');
var util = require('./util');

var MONGO_DB_CONNECTION_ERROR_CODE = 10;
var MONGO_DB_REQUEST_ERROR_CODE = 9;

var MONGO_DB_CONNECTION_ERROR_OBJECT = {'errorCode': MONGO_DB_CONNECTION_ERROR_CODE};
/**
 * [description]
 *
 * @param  {Object} groupCollection reference to the database collection
 * @param  {JSONObject} groupData   group to be inserted into the database
 *                                  {String} 'name'
 *                                  {String} 'imageUrl'
 *                                  {JSONArray} 'users' consisting of userId Strings
 *                                  {JSONArray} 'transactions' consisting of transaction JSONObjects
 * @return {[type]}                 [description]
 */
group.createNewGroup = function(groupCollection, groupData) {
  return new Promise((resolve, reject) => {
    var responseData = {};
    groupData.objectId = util.generateUUID();
    groupData.createdAt = new Date();

    groupCollection.insert(groupData, function(err, result) {
      responseData.payload = {};
      if (err != null) {
        responseData.payload.message = 'Error: ' + err;
        responseData.success = false;

        winston.info('Creating a new group failed ');
        reject(responseData);
      } else {
        responseData.payload = groupData;
        responseData.success = true;

        delete responseData.payload._id;

        winston.info('Creating a new group successful');
        resolve(responseData);
      }
    });
  });
};
