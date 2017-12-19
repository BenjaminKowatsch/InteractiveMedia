var settings = {
    "jwtSimpleSecret": "${JWT_SIMPLE_SECRET}",
    "webServiceURL": "${WEB_SERVICE_URL}",
    // "webServiceProdURL": "${WEB_SERVICE_PROD_URL}",
    "googleParams": {
        "client_id": "${CLIENT_ID}"
    },
    //Without the facebookParams the webpage doesnt load 
    "facebookParams": {
        "appId": "${APP_ID}",
        "version": "${FACEBOOK_API_VERSION}"
    },
    "origin": "${ORIGIN_URL}"
};
module.exports = settings;