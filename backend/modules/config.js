// Define configuration options in separate node module
var settings = {
  'port': process.env.PORT,
  'mongodbURL': process.env.MONGODB_URL,
  'origin': process.env.ORIGIN,
  'googleOAuthClientID': process.env.GOOGLE_OAUTH_CLIENT_ID,
  'facebookUrlAppToken': process.env.FACEBOOK_URL_APP_TOKEN,
  'jwtSimpleSecret': process.env.JWT_SIMPLE_SECRET
};
module.exports = settings;
