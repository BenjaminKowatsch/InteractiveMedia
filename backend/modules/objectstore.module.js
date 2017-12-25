const winston = require('winston');
const config = require('../modules/config');
const Minio = require('minio');
const ERROR = require('../config.error');

const minioClient = new Minio.Client({
  endPoint: config.minioEndpoint,
  port: config.minioEndpointPort,
  secure: false,
  accessKey: config.minioAccessKey,
  secretKey: config.minioSecretKey
});

minioClient.bucketExists(config.minioBucketName, function(err) {
  winston.debug('in bucketExists');
  if (err) {
    if (err.code === 'NoSuchBucket') {
      winston.debug('Minio bucket ' + config.minioBucketName + ' will be created.');
      minioClient.makeBucket(config.minioBucketName, 'us-east-1', function(err) {
        if (err) {
          return winston.error(err);
        }
        winston.debug('Bucket created successfully in "us-east-1".');
      });
    } else {
      winston.debug('An error occured');
    }
  } else {
    winston.debug('Minio bucket ' + config.minioBucketName + ' already exists.');
  }
});

module.exports.putObject = function(bucketName, objectName, stream) {
    return new Promise((resolve, reject) => {
      winston.debug('storing file: ' + objectName + ' at bucket: ' + bucketName);
      let responseData = {payload: {}};

      minioClient.putObject(bucketName, objectName, stream).then(arg => {
        responseData.success = true;
        resolve(responseData);
      }).catch((err, etag) => {
        winston.debug('minio put error:', JSON.stringify(err));
        responseData.success = false;
        responseData.payload.dataPath = 'objectstore';
        responseData.payload.message = 'failed to store file';
        let errorCode = ERROR.MINIO_ERROR;
        reject({errorCode: errorCode, responseData: responseData});
      });
    });
  };

module.exports.getObject = function(bucketName, objectName) {
    return new Promise((resolve, reject) => {
        winston.debug('loading file: ' + objectName + ' at bucket: ' + bucketName);
        let responseData = {payload: {}};

        minioClient.getObject(bucketName, objectName).then(stream => {
          responseData.success = true;
          responseData.payload.stream = stream;
          resolve(responseData);
        }).catch((err, etag) => {
          responseData.success = false;
          responseData.payload.dataPath = 'objectstore';
          responseData.payload.message = 'failed to load file';
          let errorCode = ERROR.MINIO_ERROR;
          reject({errorCode: errorCode, responseData: responseData});
        });
      });
  };
