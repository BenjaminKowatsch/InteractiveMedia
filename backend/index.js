/**
 * imports
 * ===================
 */
var express = require('express');
var bodyParser = require('body-parser');
var morgan = require('morgan');

var helloRoutes = require('./routes/hello');
var usersRoutes = require('./routes/users.routes');
var versionRoutes = require('./routes/version.routes');
var groupRoutes = require('./routes/groups.routes');

var config = require('./modules/config');
var user = require('./modules/user');
var database = require('./modules/database');
var emotionData = require('./modules/emotionData');
var userDataInit = require('./modules/init_user');

var requestValidator = require('./services/validateHttpRequestData');

// Require schemata (Load the schema files once)
var jsonSchema = {
  userData: require('./JSONSchema/userData.json'),
  googleFacebookLogin: require('./JSONSchema/googleFacebookLogin.json'),
  accessToken: require('./JSONSchema/accessToken.json'),
  postData: require('./JSONSchema/postData.json'),
  getStatistics: require('./JSONSchema/getStatistics.json')
};

var MONGO_DB_CONNECTION_ERROR_CODE = 10;

/**
 * Database connection
 * ===================
 */
database.tryConnect(config.mongodbURL, function() {
  var createIndexCallback = function(err, indexname) {
    if (err === null) {
      console.log('Created index + ' + indexname);
    } else {
      console.log('Creation of index + ' + indexname + ' failed');
    }
  };

  database.collections.users.createIndex({
    username: 1,
    password: 1
  }, {
    unique: true,
    partialFilterExpression: {
      username: {
        '$exists': true
      },
      password: {
        '$exists': true
      }
    }
  }, createIndexCallback);

  database.collections.users.createIndex({
    userId: 1,
    loginType: 1
  }, {
    unique: true
  }, createIndexCallback);

  database.collections.launometerUsers.createIndex({
    username: 1,
    password: 1
  }, {
    unique: true
  }, createIndexCallback);

  database.collections.launometerUsers.createIndex({
    userId: 1
  }, {
    unique: true
  }, createIndexCallback);

  console.log('Inserting default data');
  database.collections.emotionData.remove({}).then(function() {
    console.log('Removed all documents from emotionData');
    return database.collections.emotionData.insertMany(emotionData.data);
  }).then(function() {
    console.log('Added all documents to emotionData');
  }).catch(function() {
    console.log('Error during Inserting default data');
  });

  database.collections.launometerUsers.remove({username: 'admin'})
    .then(function() {
      console.log('Removed admin document from userdata');
      return database.collections.launometerUsers.insertMany(userDataInit.data);
    }).then(function() {
      console.log('Added admin document to userData');
    }).catch(function() {
      console.log('Error during Inserting default data');
    });
}, function() {
  console.log('Not connected to database after maxRetries reached.');
});

/**
 * Initialize express instance
 * ===========================
 */
var app = express();
var server;

// initialize logger
app.use(morgan('combined'));

app.use(bodyParser.json());

// Add headers
app.use(function(req, res, next) {
  // Website you wish to allow to connect
  res.setHeader('Access-Control-Allow-Origin', config.origin);

  // Request methods you wish to allow
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE');

  // Request headers you wish to allow
  res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type');

  // Set to true if you need the website to include cookies in the requests sent
  // to the API (e.g. in case you use sessions)
  res.setHeader('Access-Control-Allow-Credentials', true);

  // Pass to next layer of middleware
  next();
});

app.use('/hello', helloRoutes);
app.use('/v1/users', usersRoutes);
app.use('/v1/version', versionRoutes);
app.use('/v1/groups', groupRoutes);

// Starts the http server and prints out the host and the port
server = app.listen(config.port, function() {
  var host = server.address().address;
  var port = server.address().port;
  console.log('Server listening on http://%s:%s', host, port);
});

/**
 * ???????????????????
 * ===================
 */

/**
 * Function returns a callback handler for sending a responseData object
 *
 * @param  {Object} res   The response object of the REST method
 * @return {function}     A callback function to be used for sending a responseData object
 */
function getSendResponseDataCallback(res) {
  return function(responseData) {
    console.log('Sending: ' + JSON.stringify(responseData));
    res.send(responseData);
  };
}

/**
 * This function is used to return a login callback handler based on the authentication type.
 *
 * @param  {user.AUTH_TYPE} authType An enumeration value, which specifies the current type of authentication
 * @return {function}          A callback to handle the login behavior for google or facebook users
 */
function getGoogleFacebookLoginHandler(authType) {
  return function(userCollection, expiryDate, userId, req, res, responseData) {
    if (user.AUTH_TYPE.GOOGLE === authType) {
      console.log('Google Login: expiryDate: ' +
        expiryDate + ' userId: ' + userId);
    } else if (user.AUTH_TYPE.FACEBOOK === authType) {
      console.log('Facebook Login: expiryDate: ' +
        expiryDate + ' userId: ' + userId);
    }
    user.googleOrFacebookLogin(userCollection, responseData, userId, expiryDate,
       authType, req.body.accessToken)
    .then(getSendResponseDataCallback(res))
    .catch(getSendResponseDataCallback(res));
  };
}

/**
 * Function used to register post methods only available for logged in users.
 *
 * @param  {string} path Path to register REST function
 * @param  {JSONObject} jsonSchema JSON schema to validate incoming request data
 * @param  {function} onSuccessCallback Callback to be called if input validation and verification of the accessToken were successful
 *         {Object} userCollection Reference to the database collection based on the authentication type
 *         {Date} expiryDate Date to indicate the expiration of the accessToken
 *         {String} userId String to uniquely identify the user
 *         {Object} req Request object of the REST method
 *         {Object} res Response object of the REST method
 *         {JSONObject} responseData Data object created during the request data validation containing the result
 *                                          This object represents the data to be send to the client
 * @param  {user.AUTH_TYPE} [staticAuthType=null] Optional value, if set it will override the authType in the request
 *                                                This value is used when registering the google and facebook login handlers,
 *                                                because in those cases the request data does not contain a authentication type
 */
function registerLoggedInPostMethod(path, jsonSchema,
   onSuccessCallback, staticAuthType = null) {
  registerPostMethodWithInputValidation(path,
    jsonSchema,
    function(res, req,
        responseData, authType) {
      verifyAccessToken(req.body.accessToken, authType)
    .then(function(promiseData) {
        console.log('Verification of access token succeeded: ');
        onSuccessCallback(promiseData.userCollection, promiseData.expiryDate,
             promiseData.userId, req, res, responseData);
      })
    .catch(function(error) {
        console.log('Error: ' + JSON.stringify(error));
        if (undefined !== error.errorCode && MONGO_DB_CONNECTION_ERROR_CODE === error.errorCode) {
          responseData.success = false;
          responseData.errorCode = MONGO_DB_CONNECTION_ERROR_CODE;
          console.log('ErrorCode: ' + MONGO_DB_CONNECTION_ERROR_CODE);
          res.send(responseData);
        } else {
          responseData.success = false;
          responseData.invalidAccessToken = true;
          console.log('Logout failed, sending: ' + JSON.stringify(responseData));
          res.send(responseData);
        }
      });
    }, staticAuthType);
}

/**
 * Function to register a POST method with integrated request data validation
 *
 * @param  {string} path Path to register REST function
 * @param  {JSONObject} jsonSchema JSON schema to validate incoming request data
 * @param  {function} onSuccessCallback Callback to be called if input validation and verification of the accessToken were successful
 *         {Object} reqy Request object of the REST method
 *         {Object} res Response object of the REST method
 *         {JSONObject} responseData Data object created during the request data validation containing the result
 *         {user.AUTH_TYPE} authType An enumeration value, which specifies the current type of authentication
 * @param  {user.AUTH_TYPE} [staticAuthType=null] Optional value, if set it will override the authType in the request
 *                                                This value is used when registering the google and facebook login handlers,
 *                                                because in those cases the request data does not contain a authentication type
 */

function registerPostMethodWithInputValidation(path, jsonSchema,
  onSuccessCallback, staticAuthType = null) {
  app.post(path, function(req, res) {
    console.log('Post path: ' + path + ' data: ' + JSON.stringify(req.body));
    requestValidator.validateRequestData(req, res, jsonSchema, function(responseData) {
      // Use static authType if set
      var authType = (staticAuthType !== null) ? staticAuthType : req.body.authType;
      onSuccessCallback(res, req, responseData, authType);
    });
  });
}

/**
 * Function to verify each type of access token (google, facebook or launometer)
 *
 * @param  {String} token    AccessToken to be verified
 * @param  {user.AUTH_TYPE} authType Authentication type specifing whether the user belongs to google, facebook or launometer authentication
 * @return {Promise}          then: {JSONObject} promiseData JSON object containing the following properties:
 *                                                 {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 {String} userId String to uniquely identify the user
 *                            catch: {JSONObject} error JSON object containing the following properties:
 */
function verifyAccessToken(token, authType) {
  switch (authType) {
    case user.AUTH_TYPE.LAUNOMETER:
      console.log('Verifing Launometer access token');
      // Verify launometer access token
      return user.verifyLaunometerAccessToken(database.collections.launometerUsers, token);
    case user.AUTH_TYPE.GOOGLE:
      console.log('Verifing Google access token');
      // Verify google access token
      return user.verifyGoogleAccessToken(database.collections.googleUsers, token);
    case user.AUTH_TYPE.FACEBOOK:
      console.log('Verifing Facebook access token');
      // Verify facebook access token
      return user.verifyFacebookAccessToken(database.collections.facebookUsers, token);
    default:
      console.log('Error: unknown authType');
      return Promise.reject('Unknown authType');
  }
}

/**
 * register routes
 * ===============
 */

registerPostMethodWithInputValidation('/launometer_login', jsonSchema.userData,
function(res, req, responseData, authType) {
  user.launometerLogin(database.collections.launometerUsers,
    responseData, req.body.username, req.body.password)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerPostMethodWithInputValidation('/register', jsonSchema.userData,
function(res, req, responseData, authType) {
  user.register(database.collections.launometerUsers,
   responseData, req.body.username, req.body.password)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/google_login', jsonSchema.googleFacebookLogin,
getGoogleFacebookLoginHandler(user.AUTH_TYPE.GOOGLE), user.AUTH_TYPE.GOOGLE);

registerLoggedInPostMethod('/facebook_login', jsonSchema.googleFacebookLogin,
getGoogleFacebookLoginHandler(user.AUTH_TYPE.FACEBOOK), user.AUTH_TYPE.FACEBOOK);

registerLoggedInPostMethod('/logout', jsonSchema.accessToken,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.logout(userCollection, responseData, userId, req.body.authType)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/get_max_val', jsonSchema.accessToken,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.getMaxValue(userCollection, responseData, userId)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/get_stat', jsonSchema.getStatistics,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.getStatistics(userCollection, responseData, userId, req.body.payload.min)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/get_diary', jsonSchema.accessToken,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.getDiary(userCollection, responseData, userId)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/insertData', jsonSchema.postData,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.createPost(userCollection, responseData, userId, req.body.payload)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/getEmotion', jsonSchema.accessToken,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.getEmotion(database.collections.emotionData, responseData)
 .then(getSendResponseDataCallback(res))
 .catch(getSendResponseDataCallback(res));
});

registerLoggedInPostMethod('/getCurrentMood', jsonSchema.accessToken,
function(userCollection, expiryDate, userId, req, res, responseData) {
  user.getCurrentMood(userCollection, database.collections.emotionData, responseData, userId)
    .then(getSendResponseDataCallback(res))
    .catch(getSendResponseDataCallback(res));
});
