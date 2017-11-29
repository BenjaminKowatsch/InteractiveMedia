var winston = require('winston');
var config = require('../modules/config');
var Minio = require('minio');
/*
var minioClient = new Minio.Client({
  endPoint: config.minioEndpoint,
  port: Number(config.minioEndpointPort),
  secure: false,
  accessKey: config.minioAccessKey,
  secretKey: config.minioSecretKey
});

minioClient.makeBucket('europetrip', 'us-east-1', function(err) {
  if (err) {
    return console.log(err);
  }
  console.log('Bucket created successfully in "us-east-1".');
});
*/
exports.upload = function() {

};
