'use strict';

/**
 * imports
 * ===================
 */
const express = require('express');
const bodyParser = require('body-parser');
const winston = require('winston');
const expressWinston = require('express-winston');

const usersRoutes = require('./routes/users.routes');
const objectStoreRoutes = require('./routes/object.store.routes');
const versionRoutes = require('./routes/version.routes');
const groupRoutes = require('./routes/groups.routes');
const statusRoutes = require('./routes/status.routes');
const testRoutes = require('./routes/test.routes');
const adminRoutes = require('./routes/admin.routes');

const config = require('./modules/config');
const user = require('./modules/user.module');
const database = require('./modules/database.module');
const objectstore = require('./modules/objectstore.module');

const pushNotificationService = require('./services/pushNotification.service');

const ERROR = require('./config.error');
const ROLES = require('./config.roles');

const MONGO_DB_CONNECTION_ERROR_CODE = 10;

/**
 * Configure logger
 * ===========================
 */
winston.level = config.logLevel;
winston.info('logLevel', winston.level);

/**
 * Initialize express instance
 * ===========================
 */
const app = express();
let server;

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
app.use('/v1/test', testRoutes);
app.use('/v1/admin', adminRoutes);

// error handling: unknown routes
// this has to be last route to be added, otherwise it will not work
app.all('*', function(req, res, next) {
  let err = new Error();
  err.status = 404;
  next(err);
});

app.use(function(err, req, res, next) {
  if (err.status !== 404) {
    return next();
  }
  const dataPath = req.method + ' ' + req.url;
  const notFoundResponse = {
    'success': false,
    'payload': {
      'message': 'Not found',
      'dataPath': dataPath
    }
  };
  winston.error('Endpoint not found:' + dataPath);
  res.status(404).send(notFoundResponse);
});

function startServer() {
  // Starts the http server and prints out the host and the port
  server = app.listen(config.port, function() {
    const host = server.address().address;
    const port = server.address().port;
    winston.info('Server listening on http://%s:%s', host, port);
  });
}

/**
 * Database connection
 * ===================
 */
database.tryConnect(config.mongodbURL, function() {
  pushNotificationService.initFcm()
  .then(() => objectstore.makeBucket(config.minioBucketName))
  .then(() => user.register(config.adminUsername, config.adminPassword, config.adminEmail, ROLES.ADMIN))
  .then(registerResult => {
    winston.info('register admin successful');
    startServer();
  }).catch(errorResult => {
    winston.error(JSON.stringify(errorResult));
    switch (errorResult.errorCode) {
      case ERROR.DUPLICATED_USER:
        // admin already exists, start server anyway
        startServer();
        break;
      case ERROR.DB_ERROR:
      case ERROR.MINIO_ERROR:
        let statusCode = 500;
        process.exit(1);
        break;
      default:
        process.exit(1);
    }
  });
}, function() {
  winston.error('Not connected to database after maxRetries reached.');
});
