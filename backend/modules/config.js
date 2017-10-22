// Define configuration options in separate node module
var settings = {
  "port" : 8081,
  "mongodbURL" : "mongodb://mongo/launometer",
  "mongodbTestURL" : "mongodb://mongo/test",
// dev-only:
//  "origin" : "http://localhost:8080"
// production:
  "origin" : "http://localhost:8080",
  "googleOAuthClientID": "483987638756-eg6ba9fiqro5fne5taq9qciptbl6dlkr.apps.googleusercontent.com",
  "facebookAppToken": "783033221863758|K0XxUC6Dc7Q-wkoAhFC1681IOzU",
  "jwtSimpleSecret": "A3xFr93Yl2qTn5"
};
module.exports = settings;
