var winston = require('winston');
var config = require('../modules/config');
var Minio = require('minio');

var minioClient = new Minio.Client({
  endPoint: config.minioEndpoint,
  port: config.minioEndpointPort,
  secure: false,
  accessKey: config.minioAccessKey,
  secretKey: config.minioSecretKey
});
/*
minioClient.bucketExists(config.minioBucketName, function(err) {
  if (err) {
    if (err.code === 'NoSuchBucket') {
      winston.info('Minio bucket ' + config.minioBucketName + ' will be created.');
      minioClient.makeBucket('europetrip', 'us-east-1', function(err) {
        if (err) {
          return winston.error(err);
        }
        winston.info('Bucket created successfully in "us-east-1".');
      });
    }
  } else {
    winston.info('Minio bucket ' + config.minioBucketName + ' already exists.');
  }
});
*/
exports.upload = function(req, res) {
  winston.info('upload called');
  minioClient.putObject(config.minioBucketName, req.file.originalname, req.file.buffer, function(error, etag) {
    winston.info('minio putObject callback');
    if (error) {
      return winston.error(error);
    }
    res.send(req.file);
  });
};

exports.download = function(req, res) {
  winston.info('download called');
  minioClient.getObject(config.minioBucketName, req.query.filename, function(error, stream) {
    if (error) {
      return res.status(500).send(error);
    }
    stream.pipe(res);
  });
};
