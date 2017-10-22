var user = module.exports = {}

/* Application configuration */
var config = require('./config')
/* JSON Web Token to create access tokens */
var jwt = require('jwt-simple')
var https = require('https')
var querystring = require('querystring')
/* Google Auth Library */
var GoogleAuth = require('google-auth-library')
var auth = new GoogleAuth()
var client = new auth.OAuth2(config.googleOAuthClientID, '', '')

var MONGO_DB_CONNECTION_ERROR_CODE = 10
var MONGO_DB_REQUEST_ERROR_CODE = 9

var MONGO_DB_CONNECTION_ERROR_OBJECT = { 'errorCode': MONGO_DB_CONNECTION_ERROR_CODE}

/* Month Names */
var MONTH_NAMES_GER = ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
  'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'
]

var AUTH_TYPE = {
  'LAUNOMETER': 0,
  'GOOGLE': 1,
  'FACEBOOK': 2
}

/**
 * Gets a date one hour from now
 * @return {Date} A date one hour in the future
 */
function getNewTokenExpiryDate () {
  var time = new Date().getTime()
  time += 3600000
  return new Date(time)
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

user.AUTH_TYPE = AUTH_TYPE

/**
 * Function to verify an access token from google.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: @param {JSONObject} promiseData Containing the following properties:
 *                                                 @param {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 @param {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 @param {String} userId String to uniquely identify the user
 *                                  catch: @param {JSONObject} error Containing the following properties:
 *                                                 @param {String} message String containing the error message
 *                                                OR
 *                                                Google Error
 */
user.verifyGoogleAccessToken = function (userCollection, token) {
  return new Promise((resolve, reject) => {
    if (undefined === userCollection) {
      reject(MONGO_DB_CONNECTION_ERROR_OBJECT)
    } else {
      client.verifyIdToken(token, config.googleOAuthClientID,
      function (error, login) {
        if (error === null) {
          var payload = login.getPayload()
          var userId = payload.sub
          var expiryDate = new Date(payload.exp * 1000)
          var promiseData = {
            userCollection: userCollection,
            expiryDate: expiryDate,
            userId: userId
          }
          resolve(promiseData)
        } else {
          reject(error)
        }
      })
    }
  })
}
/**
 * Function to verify an own access token from launometer.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: @param {JSONObject} promiseData Containing the following properties:
 *                                                 @param {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 @param {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 @param {String} userId String to uniquely identify the user
 *                                  catch: @param {JSONObject} error Containing the following properties:
 *                                                 @param {String} message String containing the error message
 *                                                OR
 *                                                MongoDB Error
 */
user.verifyLaunometerAccessToken = function (userCollection, token) {
  return new Promise((resolve, reject) => {
    if (undefined === userCollection) {
      reject(MONGO_DB_CONNECTION_ERROR_OBJECT)
    } else {
      var payload = jwt.decode(token, config.jwtSimpleSecret)
      var query = {
        userId: payload.userId,
        expiryDate: {
          '$gt': new Date()
        }
      }
      var options = { fields: { userId: 1, expiryDate: 1 } }
      userCollection.findOne(query, options, function (error, result) {
        if (error === null && result !== null) {
          var promiseData = {
            userCollection: userCollection,
            expiryDate: result.expiryDate,
            userId: result.userId
          }
          resolve(promiseData)
        } else {
          reject(error)
        }
      })
    }
  })
}
/**
 * Function to verify a access token from facebook.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {String} token    AccessToken to be verified
 * @return {Promise}                then: @param {JSONObject} promiseData Containing the following properties:
 *                                                 @param {Object} userCollection  Reference to the database collection based on the authentication type
 *                                                 @param {Date} expiryDate Date to indicate the expiration of the accessToken
 *                                                 @param {String} userId String to uniquely identify the user
 *                                  catch: @param {JSONObject} error Containing the following properties:
 *                                                 @param {String} message String containing the error message
 *                                                OR
 *                                                Facebook Error
 */
user.verifyFacebookAccessToken = function (userCollection, token) {
  return new Promise((resolve, reject) => {
    if (undefined === userCollection) {
      reject(MONGO_DB_CONNECTION_ERROR_OBJECT)
    } else {
      var options = {
        host: 'graph.facebook.com',
        path: ('/v2.9/debug_token?access_token=' + config.facebookAppToken + '&input_token=' + token)
      }

      https.get(options, function (response) {
        var responseMessage = ''

        response.on('data', function (chunk) {
          responseMessage += chunk
        })

        response.on('end', function () {
          var data = JSON.parse(responseMessage)
          if (data.error !== undefined) {
            reject(data.error)
          } else {
            var userId = data.data.user_id
            var expiryDate = new Date(data.data.expires_at * 1000)
            var promiseData = {
              userCollection: userCollection,
              expiryDate: expiryDate,
              userId: userId
            }
            resolve(promiseData)
          }
        })
      })
    }
  })
}

/**
 * Function to login either a google or a facebook user.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 *                                  Will be used to save the result from this login function.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @param {Date} expiryDate Date to indicate the expiration of the accessToken, will be stored into the database
 * @param  {user.AUTH_TYPE} authType An enumeration value, which specifies the current type of authentication,
 *                                   to be stored into the responseData, so the client will received it and store it into a cookie
 * @param  {String} accessToken    AccessToken to be stored into the responseData, so the client will received it and store it into a cookie
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {JSONObject} payload
 */
user.googleOrFacebookLogin = function (userCollection, responseData, userId, expiryDate, authType, accessToken) {
  return new Promise((resolve, reject) => {
    // Upsert entry at db
    userCollection.updateOne({
      'userId': userId
    }, {
      'userId': userId,
      'expiryDate': expiryDate
    }, {
      upsert: true
    },
      function (err, result) {
        if (err !== null) {
          responseData.success = false
          console.log('Login failed')
          reject(responseData)
        } else {
          responseData.payload = {}
          responseData.payload.authType = authType
          responseData.payload.accessToken = accessToken
          console.log('Login successful ')
          resolve(responseData)
        }
      })
  })
}

/**
 * Function to login a launometer user.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 *                                  Will be used to save the result from this login function.
 * @param  {String} username       The name of the user
 * @param  {String} password       The password of the user
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {JSONObject} payload
 */
user.launometerLogin = function (userCollection, responseData, username, password) {
  return new Promise((resolve, reject) => {
    if (undefined === userCollection) {
      responseData.success = false
      responseData.errorCode = MONGO_DB_CONNECTION_ERROR_CODE
      console.log('Error code: ' + MONGO_DB_CONNECTION_ERROR_CODE)
      reject(responseData)
    } else {
      var newExpiryDate = getNewTokenExpiryDate()
      var query = {
        'username': username,
        'password': password
      }
      var update = {
        '$set': {
          'expiryDate': newExpiryDate
        }
      }
      var options = {
        projection: {
          userId: 1,
          expiryDate: 1
        },
        returnOriginal: false
      }

      userCollection.findOneAndUpdate(query, update, options, function (err, result) {
        if (err === null && result.value !== null && result.ok === 1) {
          responseData.payload = {}
          console.log(result.value)
          // Successfully logged in and created new expiry date
          // Generate Access Token
          // Remove the database id from the json object
          delete result.value._id
          responseData.payload.authType = AUTH_TYPE.LAUNOMETER
          responseData.payload.accessToken = jwt.encode(result.value, config.jwtSimpleSecret)
          console.log('Login successful ')
          resolve(responseData)
        } else {
          // Error handling
          responseData.success = false
          console.log('Login failed ')
          resolve(responseData)
        }
      })
    }
  })
}
/**
 * Function to logout a user independent of the authType
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {JSONObject} payload
 */
user.logout = function (userCollection, responseData, userId) {
  return new Promise((resolve, reject) => {
    var update = {
      '$set': {
        'expiryDate': new Date()
      }
    }
    var query = { 'userId': userId }
    userCollection.updateOne(query, update, function (err, result) {
      // Driver returns result as json string, not an object, so the json string has to be parsed into an object
      result = JSON.parse(result)
      if (err === null && result.n === 1 && result.ok === 1) {
        // Successfully updated the expiryDate
        console.log('Logout successful')
        resolve(responseData)
      } else {
        responseData.success = false
        console.log('Logout failed ')
        reject(responseData)
      }
    })
  })
}
/**
 * Function to register a new launometer user at the database
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param  {String} username       The name of the new user
 * @param  {String} password       The password of the new user
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.register = function (userCollection, responseData, username, password) {
  return new Promise((resolve, reject) => {
    var userData = {}
    if (undefined === userCollection) {
      responseData.success = false
      responseData.errorCode = MONGO_DB_CONNECTION_ERROR_CODE
      console.log('Error code: ' + MONGO_DB_CONNECTION_ERROR_CODE)
      reject(responseData)
    } else {
      console.log('User will be created')
    // Setup userData
      userData.expiryDate = getNewTokenExpiryDate() // now + 1h
      userData.password = password
      userData.username = username
      userData.userId = generateUUID()

     
      userCollection.insertOne(userData, function (err, result) {
        
        
        responseData.payload = {}
        if (err != null && err.code === 11000) {
          responseData.payload.dataPath = 'username'
          responseData.payload.message = 'Username already exists'
          responseData.success = false
         
          console.log('Registration/Login failed ')
          reject(responseData)
        } else {
          
          responseData.payload = {}
          var payload = {
            'userId': userData.userId,
            'expiryDate': userData.expiryDate
          }
         
          responseData.payload.accessToken = jwt.encode(payload, config.jwtSimpleSecret)
          responseData.payload.authType = AUTH_TYPE.LAUNOMETER
          console.log('Registration/Login successful')
          resolve(responseData)
        }
        
      })
    }
  })
}
/**
 * Function to get the maximal statistic value.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.getMaxValue = function (userCollection, responseData, userId) {
  return new Promise((resolve, reject) => {
    userCollection.find({
      userId: userId
    }, {
      posts: 1
    }).toArray().then(function (docs) {
      var data_exists
      var maxVal
      var values = []

      if (docs.length > 0) {
        data_exists = true
        maxVal = docs[0].posts.length
        values.push(data_exists)
        values.push(maxVal)
      } else {
        data_exists = false
        maxVal = 0
        values.push(data_exists)
        values.push(maxVal)
      }
      responseData.payload = values
      resolve(responseData)
    }).catch(function () {
      responseData.success = false
      responseData.errorCode = MONGO_DB_REQUEST_ERROR_CODE
      console.log('Error code: ' + MONGO_DB_REQUEST_ERROR_CODE)
      reject(responseData)
    })
  })
}

/**
 * Function to get five statistic values.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @param {Number} minValue A number to specify how many entries at the database shall be skipped
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.getStatistics = function (userCollection, responseData, userId, minValue) {
  return new Promise((resolve, reject) => {
    userCollection.find({
      userId: userId
    }, {
      posts: {
        $slice: [minValue, 5]
      }
    }).toArray().then(function (docs) {
      var labels = []
      var colors = []
      var percentage = []
      var data_exists
      var moodData = new Array(labels, colors, percentage)
      if (docs.length > 0) {
        for (var i = 0; i < docs[0].posts.length; i++) {
          var year = JSON.stringify(new Date(docs[0].posts[i].date).getFullYear())
          var month = JSON.stringify(new Date(docs[0].posts[i].date).getMonth() + 1)
          var day = JSON.stringify(new Date(docs[0].posts[i].date).getDate())

          var date = day.concat('.', month, '.', year)

          data_exists = true
          labels.push(date)
          colors.push(docs[0].posts[i].colour.toString())
          percentage.push(JSON.stringify(docs[0].posts[i].mood))
          moodData.push(data_exists)
        }
      } else {
        data_exists = false
        moodData.push(data_exists)
      }
      responseData.payload = moodData
      resolve(responseData)
    }).catch(function () {
      responseData.success = false
      responseData.errorCode = MONGO_DB_REQUEST_ERROR_CODE
      console.log('Error code: ' + MONGO_DB_REQUEST_ERROR_CODE)
      reject(responseData)
    })
  })
}
/**
 * Function to get all posts from the database.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.getDiary = function (userCollection, responseData, userId) {
  return new Promise((resolve, reject) => {
    var diaryEntry = []
    var data_exists
    var data = []
    userCollection.aggregate([{$match: { userId: userId }}, {$project: {
      posts: {$filter: {
        input: '$posts',
        as: 'post',
        cond: {$ne: ['$$post.text', null]}
      }}
    }}], function (error, result) {
      if (error === null) {
        if (result[0].posts.length > 0) {
          for (var i = 0; i < result[0].posts.length; i++) {
            if (result[0].posts[i].text !== null) {
              var entry = JSON.stringify({
                day: new Date(result[0].posts[i].date).getDate().toString(),
                month: MONTH_NAMES_GER[new Date(result[0].posts[i].date).getMonth()],
                message: result[0].posts[i].text
              })

              data_exists = true

              diaryEntry.push(JSON.parse(entry))
            }
          }
          data.push(data_exists)
          data.push(diaryEntry)
        } else {
          data_exists = false
          data.push(data_exists)
        }
        responseData.payload = data
        resolve(responseData)
      } else {
        console.log('Error: ' + JSON.stringify(error))
        responseData.success = false
        reject(responseData)
      }
    })
  })
}
/**
 * Function to store a post at the database.
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @param {JSONObject} post JSON object containing the following values:
 *                    @param {Number} date A timestamp specifing the date of the post
 *                    @param {Number} mood A number specifing the mood of user
 *                    @param {String} emotion A String containing an emotion
 *                    @param {String} colour A HEX String of a color for the specified emotion
 *                    @param {String} text An optional text to describe the current mood
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.createPost = function (userCollection, responseData, userId, post) {
  return new Promise((resolve, reject) => {
    var query = {
      userId: userId
    }
    var update = {
      $addToSet: {
        posts: {
          date: post.date,
          mood: parseInt(post.mood, 10),
          emotion: post.emotion,
          colour: post.colour,
          text: post.text
        }
      }
    }

    userCollection.update(query, update,
    function (err, result) {
      if (err === null && result.result.nModified === 1) {
        console.log(result.result.nModified + ' record updated')
        resolve(responseData)
      } else {
        responseData.success = false
        console.log('Inerting data failed ')
        reject(responseData)
      }
    })
  })
}
/**
 * Gets the current mood of the user by userId. Since the posts data is in german
 * A mapping has to be applied to get the english version of the mood. Therfore a second database request is initiated
 * @param {Object} userCollection  Reference to the database collection based on the authentication type
 * @param  {Object} emotionDataCollection  Shall be the collection 'emotionData' containing the mapping of the emotion from german to english
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @param {String} userId String to uniquely identify the user, to find the user at the database
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.getCurrentMood = function (userCollection, emotionDataCollection, responseData, userId) {
  return new Promise((resolve, reject) => {
    // Find the latest mood of the user
    userCollection.aggregate([
        {$match: { userId: userId }},
        {$project: {'_id': 0, 'posts.date':1, 'posts.emotion': 1}},
        {$unwind: '$posts'},
        {$sort: {'posts.date': -1}},
        {$limit: 1}
    ], function (error, result) {
      if (error === null) {
        responseData.payload.emotion_de = result[0].posts.emotion
        // Get mapping from DE to EN emotion
        emotionDataCollection.findOne({name: result[0].posts.emotion }, function(error, result){
          if(error === null && result !== null){
            responseData.payload.emotion = result.name_en
            resolve(responseData)
          }else{
            console.log('Error: ' + JSON.stringify(error))
            responseData.success = false
            reject(responseData)
          }
        })
      } else {
        console.log('Error: ' + JSON.stringify(error))
        responseData.success = false
        reject(responseData)
      }
    })
  })
}

/**
 * Gets the emotion JSON documents at the database
 * @param  {Object} collection   Shall be the collection 'emotionData'
 * @param {JSONObject} responseData Data object created during the request data validation containing the result.
 * @return {Promise}                then: @param {JSONObject} promiseData Is a modified version of the responseData object
 *                                                 @param {Boolean} success  Flag to indicate the successful request
 *                                                 @param {JSONObject} payload
 *                                  catch: @param {JSONObject} error Is a modified version of the responseData object
 *                                                @param {Boolean} success  Flag to indicate the unsuccessful request
 *                                                @param {Number} errorCode  Enumeration to specify the error
 *                                                @param {JSONObject} payload
 */
user.getEmotion = function (collection, responseData) {
  return new Promise((resolve, reject) => {
    collection.find({}).toArray(function (err, docs) {
      if (err !== null) {
        responseData.success = false
        responseData.errorCode = MONGO_DB_REQUEST_ERROR_CODE
        console.log('Error code: ' + MONGO_DB_REQUEST_ERROR_CODE)
        reject(responseData)
      } else {
        responseData.payload = docs
        resolve(responseData)
      }
    })
  })
}
