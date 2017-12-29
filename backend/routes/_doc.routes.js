'use strict';

// jscs:disable jsDoc

/**
 * @apiDefine paramAccessToken
    @apiParam (Parameter) {string} accessToken Token to verify user
 */
/**
 * @apiDefine paramUsername
    @apiParam (Parameter) {string} username Username
 */
/**
 * @apiDefine paramHashedPassword
    @apiParam (Parameter) {string} password Hash of a password
 */
/**
 * @apiDefine paramAuthtype
    @apiParam (Parameter) {int} authType Login type with authType=(0: Password, 1:Google, 2: Facebook)
 */

/**
 * @apiDefine headerAuthorization
 * @apiHeader (HTTP-Header) {Request} Authorization Provide authentication: type and authtoken separated with one space
     type=(0: Password, 1:Google, 2: Facebook)
 */

/** @apiDefine headerExampleAuthorization
 * @apiHeaderExample {string} Authorization:
      Authorization: "0 FJF7HFkA38jF6FH9JF7"
 */

/**
 * @apiDefine successBodySuccess
    @apiSuccess (Success) {string} success Request successful
 */
/**
 * @apiDefine successBodyAuthtype
    @apiSuccess (Success) {string} payload[authtype] Login type with type=(0: Password, 1:Google, 2: Facebook)
 */
/**
 * @apiDefine successBodyAuthtoken
    @apiSuccess (Success) {string} payload[accessToken] Access token for further use
 */

/**
 * @apiDefine errorBodyCommonStructure
    @apiError (Error) {string} success Request failed
    @apiError (Error) {string} payload[dataPath] Describe the error location
    @apiError (Error) {string} payload[message] Specify the error
 */

/**
 * @apiDefine error400MissingUnknownUrlParameterType
    @apiError (ErrorCode) {400} MissingType Missing or unknown type parameter in url
 */
/**
 * @apiDefine error400MissingUnknownUrlParameter
    @apiError (ErrorCode) {400} MissingType Missing or unknown parameter in url
 */
/**
 * @apiDefine error400BadRequest
    @apiError (ErrorCode) {400} BadRequest Missing or malformed request
 */
/**
 * @apiDefine error404UnknownId
    @apiError (ErrorCode) {404} UnknownId Id for the requested resource is unknown
 */
/**
 * @apiDefine error400InvalidBody
    @apiError (ErrorCode) {400} InvalidBody Validation error for request body
 */
/**
 * @apiDefine error401Unauthorized
    @apiError (ErrorCode) {401} Unauthorized Missing or invalid authentication information
 */
/**
 * @apiDefine error401CredentialsInvalid
    @apiError (ErrorCode) {401} InvalidCredentials Credentials are invalid
 */

/**
 * @apiDefine error403Forbidden
    @apiError (ErrorCode) {403} Forbidden The authenticated user is not permitted to perform the requested operation
 */

/**
 * @apiDefine error418UncaughtError
    @apiError (ErrorCode) {418} UncaughtError Uncaught error
 */

/**
 * @apiDefine error500MinioInternalError
    @apiError (ErrorCode) {500} MinioInternalError Minio internal error
 */

/**
 * @apiDefine error500DatabaseError
    @apiError (ErrorCode) {500} DatabaseError Database internal error
 */

/**
 * @apiDefine paramExamplePassword
    @apiParamExample {type} Password
    {
        "username" : "alex",
        "password" : "XHDGETFHFJCHF"
    }
 */
/**
 * @apiDefine paramExampleLoginFacebook
    @apiParamExample {type} Facebook
    {
        "accessToken" : "DeUBZBpAr9KCBZB3knwe1eGvcur"
    }
 */
/**
 * @apiDefine paramExampleLoginGoogle
    @apiParamExample {type} Google
    {
        "TODO" : "TODO"
    }
 */
/**
 * @apiDefine paramExampleAuthtoken
    @apiParamExample {type} Authtoken
    {
        "accessToken" : "SDSHDHCS",
        "authType" : 0
    }
 */

/**
 * @apiDefine successExampleAuthtypeToken
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "authType": 0,
            "authToken": "KDFBDICBIUDSBSDhdddhf784gG7F"
        }
    }
 */
/**
 * @apiDefine successExampleSuccess
    @apiSuccessExample {type} Success-Response
    {
        "success": true
    }
 */

/**
 * @apiDefine errorExampleCommon
    @apiErrorExample {type} Error-Response
    {
        "success": false,
        "payload": {
            "dataPath": "logout",
            "message": "logout failed"
        }
    }
    @apiErrorExample {type} Error-Response
    {
        "success": false,
        "payload": {
            "dataPath": "groupUsers",
            "message": "Unknown user: wrong_mail@mail.com"
        }
    }
 */

/**
 * @apiDefine successExampleGroup
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "name" : "test_group_1",
            "imageUrl" : null,
            "users" : [{
                "userId": "f2bed6b9-6a5a-4363-a9fa-e1f10579c0c1",
                "username": "user_1_name"
            },{
                "userId": "2368218d-b5ec-4d4d-bc3c-6c249776ee11",
                "username": "user_2_name"
            }]
            "transactions" : [ ... ], // all transaction-objects, length=0 if group was just created
            "groupId" : "6367e722-e857-4d0f-bf78-278a92260418",
            "createdAt" : "2017-12-25T10:56:13.234Z"
    }
 */
/**
 * @apiDefine paramExampleCreateGroup
    @apiParamExample {type} CreateGroup
    {
        name: 'TheFooBars',
        imageUrl: null,
        users: ['user_1@example.de', 'user_2@example.de', 'user_3@example.de']
    }
 */

/**
 * @apiDefine error409UnknownUser
    @apiError (ErrorCode) {409} UnknownUser Unknown user: {{wrong_mail@mail.com}}
 */
/**
 * @apiDefine error400DuplicatedUsers
    @apiError (ErrorCode) {400} DuplicatedUsers Duplicated groupUsers
 */
/**
 * @apiDefine error400MissingGroupCreator
    @apiError (ErrorCode) {400} MissingGroupCreator GroupCreator must be part of groupUsers
 */
/**
 * @apiDefine error403Unauthorized
    @apiError (ErrorCode) {403} Unauthorized User does not have the necessary permission
 */
/**
 * @apiDefine error500UnknownUser
    @apiError (ErrorCode) {500} UnknownUser Requestd group or user references a non-existing userId
 */

/**
 * @apiDefine successExampleUser
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "username": "my_user_name",
            "email": "my_user_mail@example.com",
            "userId": "6367e722-e857-4d0f-bf78-278a92260418",
            "groupIds": ["f2bed6b9-6a5a-4363-a9fa-e1f10579c0c1","2368218d-b5ec-4d4d-bc3c-6c249776ee11"]
        }
    }
 */
/**
 * @apiDefine successBodyUsername
    @apiSuccess (Success) {string} payload[username] Username of requesting user
 */
/**
 * @apiDefine successBodyEmail
    @apiSuccess (Success) {string} payload[email] Email of requesting user
 */
/**
 * @apiDefine successBodyUserId
    @apiSuccess (Success) {string} payload[userId] Id of requesting user
 */
/**
 * @apiDefine successBodyGroupIds
    @apiSuccess (Success) {Array[string]} payload[groupIds] Ids of requesting user's groups
 */
