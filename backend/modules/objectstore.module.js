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

module.exports.makeBucket = function(bucketName) {
  return new Promise((resolve, reject) => {
    let responseData = {payload: {}};

    minioClient.bucketExists(bucketName).then(() => {
      winston.debug('bucket exists', bucketName);
      responseData.success = true;
      resolve(responseData);
    }).catch((err) => {
      if (err.code === ERROR.MINIO_NO_SUCH_BUCKET) {
        winston.debug('bucket does not exist', bucketName);
        return minioClient.makeBucket(bucketName, 'us-east-1');
      } else {
        return Promise.reject(err);
      }
    }).then(() => {
      winston.debug('bucket created', bucketName);
      responseData.success = true;
      resolve(responseData);
    }).catch((err) => {
      winston.debug('error while creating bucket:', JSON.stringify(err));
      responseData.success = false;
      responseData.payload.dataPath = 'objectstore';
      responseData.payload.message = 'unknown minio error';
      let errorCode = ERROR.MINIO_ERROR;
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
};

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
