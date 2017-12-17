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
 * @apiParam (form-data) {string} accessToken Token to verify user
 * @apiParam (form-data) {int} authType Login type with authType=(0: Password, 1:Google, 2: Facebook)
 * @apiParam (form-data) {file} uploadField File to upload
 *
 * @apiParamExample {type} form-data
    {
        "accessToken" : "SDSHDHCS",
        "authType" : 0,
        "uploadField": "<file>"
    }
 *
 * @apiSuccess (SuccessCode) {201} Created Upload image successful
 * @apiUse successBodySuccess
 * @apiSuccess (Success) {string} payload.path Path of image for further use
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
 * @apiUse error401CredentialsInvalid
 * @apiUse error500MinioInternalError
 * @apiUse errorBodyCommonStructure
 *
 * @apiUse errorExampleCommon
 */
router.post('/upload', multer({storage: multer.memoryStorage()}).single('uploadField'),
    authenticationService.isAuthenticated, objectStoreController.upload);

router.get('/download', objectStoreController.download);

module.exports = router;
