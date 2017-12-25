var winston = require('winston');
var config = require('../modules/config');
var Minio = require('minio');
const httpResponseService = require('../services/httpResponse.service');
const uuidService = require('../services/uuid.service');
const ERROR = require('../config.error');

var minioClient = new Minio.Client({
  endPoint: config.minioEndpoint,
  port: config.minioEndpointPort,
  secure: false,
  accessKey: config.minioAccessKey,
  secretKey: config.minioSecretKey
});

winston.debug('minioclient: ' + (undefined === minioClient));

minioClient.bucketExists(config.minioBucketName, function(err) {
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

function minioPutObject(bucketName, objectName, stream) {
  return new Promise((resolve, reject) => {
    winston.debug('storing file: ' + objectName + ' at bucket: ' + bucketName);
    let responseData = {payload: {}};

    minioClient.putObject(bucketName, objectName, stream).then(arg => {
      responseData.success = true;
      resolve(responseData);
    }).catch((err, etag) => {
      responseData.success = false;
      responseData.payload.dataPath = 'objectstore';
      responseData.payload.message = 'failed to store file';
      let errorCode = ERROR.MINIO_ERROR;
      reject({errorCode: errorCode, responseData: responseData});
    });
  });
}

function parseRequestUploadFile(req) {
  let responseData = {payload: {}};
  return new Promise((resolve, reject) => {
    if (!('file' in req)) {
      responseData.success = false;
      responseData.payload.dataPath = 'objectstore';
      responseData.payload.message = 'invalid or missing file';
      let errorCode = ERROR.INVALID_OR_MISSING_FILE_IN_REQUEST;
      reject({errorCode: errorCode, responseData: responseData});
    }
    const filenameRaw = req.file.originalname;
    winston.debug('filenameRaw', filenameRaw);

    if (filenameRaw == null || filenameRaw == undefined) {
      responseData.success = false;
      responseData.payload.dataPath = 'objectstore';
      responseData.payload.message = 'invalid or missing file';
      let errorCode = ERROR.INVALID_OR_MISSING_FILE_IN_REQUEST;
      reject({errorCode: errorCode, responseData: responseData});
    }

    const filename = uuidService.generateUUID() + '.' + filenameRaw;
    winston.debug('filename', filename);
    responseData.success = true;
    responseData.payload.filename = filename;
    responseData.payload.filenameRaw = filenameRaw;
    resolve(responseData);
  });
}

module.exports.upload = function(req, res) {
  let responseData = {payload: {}};
  let filename;
  parseRequestUploadFile(req).then(fileMeta => {
    filename = fileMeta.payload.filename;
    return minioPutObject(config.minioBucketName, filename, req.file.buffer);
  }).then(promiseData => {
      responseData.success = true;
      responseData.payload.path = filename;
      httpResponseService.send(res, 201, responseData);
    }).catch(errorResult => {
      winston.error('errorCode', errorResult.errorCode);
      let statusCode = 418;
      switch (errorResult.errorCode) {
        case ERROR.INVALID_OR_MISSING_FILE_IN_REQUEST:
          statusCode = 400;
          break;
        case ERROR.MINIO_ERROR:
          statusCode = 500;
          break;
      }
      httpResponseService.send(res, statusCode, errorResult.responseData);
    });
};

module.exports.download = function(req, res) {
  let errorResponse = {
    'success': false,
    'payload': {
      'dataPath': '',
      'message': ''
    }
  };

  if (req.query.filename !== null && req.query.filename !== undefined) {
    winston.debug('download file: ' + req.query.filename + ' at bucket: ' + config.minioBucketName);

    minioClient.getObject(config.minioBucketName, req.query.filename, function(error, stream) {
      if (error) {
        winston.debug('internal minio error');
        errorResponse.payload.dataPath = 'getObject';
        errorResponse.payload.message = 'failed to get object';
        httpResponseService.send(res, 500, errorResponse);
      } else {
        stream.pipe(res);
      }
    });
  } else {
    winston.debug('missing or invalid filename');
    errorResponse.payload.dataPath = 'invalidFilename';
    errorResponse.payload.message = 'missing or invalid filename';
    httpResponseService.send(res, 400, errorResponse);
  }
};
