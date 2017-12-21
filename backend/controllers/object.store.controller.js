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
  let errorResponse = {
    'success': false,
    'payload': {
      'dataPath': '',
      'message': ''
    }
  };

  if ('file' in req) {
    const filenameRaw = req.file.originalname;

    if (filenameRaw !== null && filenameRaw !== undefined) {
      const filename = uuidService.generateUUID() + '.' + filenameRaw;

      winston.debug('storing file: ' + filename + ' at bucket: ' + config.minioBucketName);
      minioClient.putObject(config.minioBucketName, filename, req.file.buffer, function(error, etag) {
        if (error) {
          winston.debug('internal minio error');
          errorResponse.payload.dataPath = 'getObject';
          errorResponse.payload.message = 'failed to get object';
          httpResonseService.sendHttpResponse(res, 500, errorResponse);
        } else {
          const successReponse = {
            'success': true,
            'payload': {
              'path': filename
            }
          };
          httpResonseService.sendHttpResponse(res, 201, successReponse);
        }
      });
    } else {
      winston.debug('invalid or missing file');
      errorResponse.payload.dataPath = 'invalidFile';
      errorResponse.payload.message = 'invalid or missing file';
      httpResonseService.sendHttpResponse(res, 400, errorResponse);
    }
  } else {
    winston.debug('invalid or missing file');
    errorResponse.payload.dataPath = 'invalidFile';
    errorResponse.payload.message = 'invalid or missing file';
    httpResonseService.sendHttpResponse(res, 400, errorResponse);
  }

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
        httpResonseService.sendHttpResponse(res, 500, errorResponse);
      } else {
        stream.pipe(res);
      }
    });
  } else {
    winston.debug('missing or invalid filename');
    errorResponse.payload.dataPath = 'invalidFilename';
    errorResponse.payload.message = 'missing or invalid filename';
    httpResonseService.sendHttpResponse(res, 400, errorResponse);
  }
};
