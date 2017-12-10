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

exports.upload = function(req, res) {
  winston.debug('storing file: ' + req.file.originalname + ' at bucket: ' + config.minioBucketName);

  minioClient.putObject(config.minioBucketName, req.file.originalname, req.file.buffer, function(error, etag) {
    winston.debug('minio putObject callback');
    if (error) {
      return winston.error(error);
    }
    res.send(req.file);
  });
};

exports.download = function(req, res) {
  winston.debug('download file: ' + req.query.filename + ' at bucket: ' + config.minioBucketName);

  minioClient.getObject(config.minioBucketName, req.query.filename, function(error, stream) {
    if (error) {
      return res.status(500).send(error);
    }
    stream.pipe(res);
  });
};
