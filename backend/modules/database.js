var database = module.exports = {}

/* MongoDB Client */
var MongoClient = require('mongodb').MongoClient

var mongoConnectConfig = {
  bufferMaxEntries: 0,
  autoReconnect: true
}

var tryConnectOptions = {
  maxRetries: 100,
  retryInterval: 500, // in milliseconds
  currentRetryCount: 0,
  url: '',
  resolve: undefined,
  reject: undefined
}

/**
 * Export access variablsefor interaction with the database
 */
database.collections = {
  'launometerUsers': undefined,
  'facebookUsers': undefined,
  'googleUsers': undefined,
  'emotionData': undefined
}
database.db = undefined

/**
 * Export the function 'tryToConnectToDatabase'
 */
database.tryConnect = function (url, resolve, reject) {
  tryConnectOptions.url = url
  tryConnectOptions.resolve = resolve
  tryConnectOptions.reject = reject
  tryToConnectToDatabase()
}
/**
 * Function which asynchronously loops until either a connection to the database could be established
 * or the maximum number of retries have been reached.
 * Attention: Callbacks are uses because default JS Promises do not support timeouts
 */
function tryToConnectToDatabase () {
  tryConnectOptions.currentRetryCount += 1
  console.log('Database connection try number: ' + tryConnectOptions.currentRetryCount)
  connect(function () {
   
    tryConnectOptions.resolve()
  }, function () {
    if (tryConnectOptions.currentRetryCount < tryConnectOptions.maxRetries) {
      setTimeout(tryToConnectToDatabase, tryConnectOptions.retryInterval)
    } else {
      tryConnectOptions.reject()
    }
  })
}

/**
 * Function to connect to mongodb, if it fails the properties at the JSON object 'collections' will be set to 'undefined'.
 * Otherwise the properties will be initialized with a reference to the corresponding collection at the database.
 * Attention: Callbacks are uses because default JS Promises do not support timeouts
 * @param  {function} resolve Called when the database connection has been established
 * @param  {function} reject  Called when the database connection could not be established
 */
function connect (resolve, reject) {
    /* Connect to mongodb once to reduce the number of connection pools created by our application  */
  
  MongoClient.connect(tryConnectOptions.url, mongoConnectConfig, function (err, db) {

    if (err === null) {
      console.log('Database connection established')
      // Initialize database and collection access variables
      database.db = db
      database.collections.launometerUsers = db.collection('launometerUsers')
      database.collections.googleUsers = db.collection('googleUsers')
      database.collections.facebookUsers = db.collection('facebookUsers')
      database.collections.emotionData = db.collection('emotionData')
      resolve()
    } else {
      console.log('Database connection failed with error: ' + err)
      database.collections.launometerUsers = undefined
      database.collections.googleUsers = undefined
      database.collections.facebookUsers = undefined
      database.collections.emotionData = undefined
      database.db = undefined
      reject()
    }
  });
}
