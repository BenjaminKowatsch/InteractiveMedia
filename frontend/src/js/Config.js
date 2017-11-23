var settings = {
  "jwtSimpleSecret": "${JWT_SIMPLE_SECRET}",
  "webServiceURL": "${WEB_SERVICE_URL}",
  "spotifyClientId": "${SPOTIFY_CLIENT_ID}",
  "googleParams":{
    "client_id": "${CLIENT_ID}"
  },
  "facebookParams":{
    "appId": "${APP_ID}",
    "version": "${FACEBOOK_API_VERSION}"
  },
  "origin" : "${ORIGIN_URL}"
};
module.exports = settings;
