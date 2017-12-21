// jscs:disable jsDoc

var express = require('express');
var multer = require('multer');
var router = express.Router();

var authenticationService = require('../services/authentication.service');
const objectStoreController = require('../controllers/object.store.controller');

/**
 * @api {POST} /v1/object-store/upload Upload
 * @apiName Upload
 * @apiGroup object-store
 * @apiVersion 0.1.0
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 * @apiParam (body) {file} uploadField File to upload
 *
 * @apiParamExample {type} body
    {
        "uploadField": "<file>"
    }
 *
 * @apiSuccess (SuccessCode) {201} Created Upload image successful
 * @apiUse successBodySuccess
 * @apiSuccess (Success) {string} payload[path] Path of image for further use
 *
 * @apiSuccessExample Success-Response
    {
        "success": true,
        "payload": {
            "path": "10896cb8-d2a4-4bb6-b4d7-c3063553fee9.Software.txt"
        }
    }
 *
 * @apiUse error400BadRequest
 * @apiUse error401Unauthorized
 * @apiUse error500MinioInternalError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/upload', multer({storage: multer.memoryStorage()}).single('uploadField'),
    authenticationService.isAuthenticated, objectStoreController.upload);

/**
 * @api {GET} /v1/object-store/download?filename=:filename Download
 * @apiName Download
 * @apiGroup object-store
 * @apiVersion 0.1.0
 *
 * @apiUse headerAuthorization
 * @apiUse headerExampleAuthorization
 * @apiParam (URL-Parameter) {string} filename filename of the desired file
 *
 * @apiSuccess (SuccessCode) {200} Successful Download file successful
 *
 * @apiUse error400BadRequest
 * @apiUse error401Unauthorized
 * @apiUse error500MinioInternalError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.get('/download', authenticationService.isAuthenticated, objectStoreController.download);

module.exports = router;
