'use strict';

const winston = require('winston');
const config = require('../modules/config');
const httpResponseService = require('../services/httpResponse.service');
const uuidService = require('../services/uuid.service');
const objectStore = require('../modules/objectstore.module');
const ERROR = require('../config.error');

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
    return objectStore.putObject(config.minioBucketName, filename, req.file.buffer);
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

function parseRequestDownloadFile(req) {
  let responseData = {payload: {}};
  if (req.query.filename == null || req.query.filename == undefined) {
    responseData.success = false;
    responseData.payload.dataPath = 'objectstore';
    responseData.payload.message = 'invalid or missing filename in request';
    let errorCode = ERROR.INVALID_OR_MISSING_FILENAME_IN_REQUEST;
    return Promise.reject({errorCode: errorCode, responseData: responseData});
  }
  const filename = req.query.filename;
  winston.debug('filename', filename);
  responseData.success = true;
  responseData.payload.filename = filename;
  return Promise.resolve(responseData);
}

module.exports.download = function(req, res) {
  let responseData = {payload: {}};
  let filename;
  parseRequestDownloadFile(req).then(fileMeta => {
    filename = fileMeta.payload.filename;
    return objectStore.getObject(config.minioBucketName, filename);
  }).then(promiseData => {
    promiseData.payload.stream.pipe(res);
  }).catch(errorResult => {
    winston.error('errorCode', errorResult.errorCode);
    let statusCode = 418;
    switch (errorResult.errorCode) {
      case ERROR.INVALID_OR_MISSING_FILENAME_IN_REQUEST:
        statusCode = 400;
        break;
      case ERROR.MINIO_ERROR:
        statusCode = 500;
        break;
    }
    httpResponseService.send(res, statusCode, errorResult.responseData);
  });
};
