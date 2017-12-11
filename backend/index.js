/**
 * imports
 * ===================
 */
var express = require('express');
var bodyParser = require('body-parser');
var winston = require('winston');
const expressWinston = require('express-winston');

var usersRoutes = require('./routes/users.routes');
var objectStoreRoutes = require('./routes/object.store.routes');
var versionRoutes = require('./routes/version.routes');
var groupRoutes = require('./routes/groups.routes');
var statusRoutes = require('./routes/status.routes');

var config = require('./modules/config');
var user = require('./modules/user');
var database = require('./modules/database');

var MONGO_DB_CONNECTION_ERROR_CODE = 10;

/**
 * Configure logger
 * ===========================
 */
winston.level = 'info';

/**
 * Initialize express instance
 * ===========================
 */
var app = express();
var server;

// initialize logger
app.use(expressWinston.logger({
  winstonInstance: winston,
  expressFormat: false,
  meta: true,
  skip: function(req, res) {
    if (req.url == '/v1/status' && req.method == 'GET' && res.statusCode == 200) {
      return true;
    }
    return false;
  }
}));

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

app.use('/v1/users', usersRoutes);
app.use('/v1/object-store', objectStoreRoutes);
app.use('/v1/version', versionRoutes);
app.use('/v1/groups', groupRoutes);
app.use('/v1/status', statusRoutes);

function startServer() {
  // Starts the http server and prints out the host and the port
  server = app.listen(config.port, function() {
    var host = server.address().address;
    var port = server.address().port;
    winston.info('Server listening on http://%s:%s', host, port);
  });
}

/**
 * Database connection
 * ===================
 */
database.tryConnect(config.mongodbURL, function() {
  var createIndexCallback = function(err, indexname) {
    if (err === null) {
      winston.debug('Created index + ' + indexname);
    } else {
      winston.debug('Creation of index + ' + indexname + ' failed');
    }
  };
  database.collections.users.createIndex({
    email: 1
  }, {
    unique: true
  }, createIndexCallback);

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

  startServer();
}, function() {
  winston.error('Not connected to database after maxRetries reached.');
});
