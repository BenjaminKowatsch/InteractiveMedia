var helper = module.exports = {}


var config = require('../modules/config')
var user = require('../modules/user')
var database = require('../modules/database')
var emotionData = require('../modules/emotionData')
var test = require('assert');
var randomMongodbURL = config.mongodbTestURL + generateUUID();



/**
 * Build up method before each test
 * Creates database, inserts necessary data, ...
 */
function up (callback) {
  console.log("Build up unit tests");

  database.tryConnect(randomMongodbURL, function () {
    console.log('Connection to database has been established');
    
    collection = database.db.collection('test');


     callback(collection);
  }, function () {
    console.log('Not connected to database after maxRetries reached.');
  });
 
}


/**
 * Teardown after each test
 * Essentially deletes the created database
 */
function down (collection) {
    console.log("Tear down Unit Test");
    onDatabaseConnected(collection);
}

function onDatabaseConnected(collection){

  collection.remove({}).then(function () {
    console.log('Removed all documents from test');
  }).then(function(){
       
    database.db.dropDatabase(function (err, result){

      test.equal(null, err);
      console.log('Random database successfully dropped');

      setTimeout(function () {
        closeDBAndExit(0);
      }, 2000);
      
    });

   
  }).catch(function () {
    console.log('Error during Inserting default data');
    closeDBAndExit(1)
  })
}




function closeDBAndExit(code){
 
  database.db.close()
  process.exit(code) // End node js with code 0, which indicates a successful end
}

/**
 * Generates a new Universal Unique Identifier
 * @return {String} Generated new Universal Unique Identifier
 */
function generateUUID () {
  var d = new Date().getTime()
  var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = (d + Math.random() * 16) % 16 | 0
    d = Math.floor(d / 16)
    return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16)
  })
  return uuid
}


module.exports.up = up;
module.exports.down = down;