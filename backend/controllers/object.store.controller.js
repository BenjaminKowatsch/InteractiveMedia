var winston = require('winston');
var config = require('../modules/config');
var Minio = require('minio');
const httpResonseService = require('../services/httpResonse.service');
const uuidService = require('../services/uuid.service');

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

module.exports.upload = function(req, res) {
  const filename = uuidService.generateUUID() + '.' + req.file.originalname;
  winston.debug('storing file: ' + filename + ' at bucket: ' + config.minioBucketName);
  minioClient.putObject(config.minioBucketName, filename, req.file.buffer, function(error, etag) {
    if (error) {
      const errorResponse = {
        'success': false,
        'payload': {
          'dataPath': 'storeObject',
          'message': 'store object failed'
        }
      };
      httpResonseService.sendHttpResponse(res, 500, errorResponse);
    } else {
      successReponse = {
        'success': true,
        'payload': {
          'path': filename
        }
      };
      httpResonseService.sendHttpResponse(res, 201, successReponse);
    }
  });
};

module.exports.download = function(req, res) {
  winston.debug('download file: ' + req.query.filename + ' at bucket: ' + config.minioBucketName);

  minioClient.getObject(config.minioBucketName, req.query.filename, function(error, stream) {
    if (error) {
      const errorResponse = {
        'success': false,
        'payload': {
          'dataPath': 'getObject',
          'message': 'failed to get object'
        }
      };
      httpResonseService.sendHttpResponse(res, 500, errorResponse);
    } else {
      stream.pipe(res);
    }
  });
};
