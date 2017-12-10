/**
 * imports
 * ===================
 */
var express = require('express');
var bodyParser = require('body-parser');
var morgan = require('morgan');

var usersRoutes = require('./routes/users.routes');
var objectStoreRoutes = require('./routes/object.store.routes');
var versionRoutes = require('./routes/version.routes');
var groupRoutes = require('./routes/groups.routes');

var config = require('./modules/config');
var user = require('./modules/user');
var database = require('./modules/database');

var MONGO_DB_CONNECTION_ERROR_CODE = 10;

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

app.use('/v1/users', usersRoutes);
app.use('/v1/object-store', objectStoreRoutes);
app.use('/v1/version', versionRoutes);
app.use('/v1/groups', groupRoutes);

function startServer() {
  // Starts the http server and prints out the host and the port
  server = app.listen(config.port, function() {
    var host = server.address().address;
    var port = server.address().port;
    console.log('Server listening on http://%s:%s', host, port);
  });
}

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

  database.collections.launometerUsers.remove({username: 'admin'})
    .then(function() {
      console.log('Removed admin document from userdata');
      return database.collections.launometerUsers.insertMany(userDataInit.data);
    }).then(function() {
      console.log('Added admin document to userData');
    }).catch(function() {
      console.log('Error during Inserting default data');
    });

  startServer();

}, function() {
  console.log('Not connected to database after maxRetries reached.');
});
