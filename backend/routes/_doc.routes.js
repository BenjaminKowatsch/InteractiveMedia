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
 * @apiDefine paramUrlGroupId
    @apiParam (URL-Parameter) {string} groupId Id of the requested group
 */
/**
 * @apiDefine paramUrlUserId
    @apiParam (URL-Parameter) {string} userId Id of the requested user
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
    @apiError (ErrorCode) {400} MissingUrlParameter Missing or unknown parameter in url
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
 * @apiDefine error500InternalServerError
    @apiError (ErrorCode) {500} InternalServerError Internal server error
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
 * @apiDefine error500FcmError
    @apiError (ErrorCode) {500} FcmError Firebase Cloud Messaging error
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
 * @apiDefine paramExampleUserUpdateFcmToken
    @apiParamExample {JSON} Request Body
    {
        fcmToken: 'asdfn489ha9hv89earaji838fae89f849h9arh9a48hf9r'
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
 * @apiDefine error500UnknownUser
    @apiError (ErrorCode) {500} UnknownUser Requestd group or user references a non-existing userId
 */

/**
 * @apiDefine successExampleSuccess
    @apiSuccessExample {type} Success-Response
    {
        "success": true
    }
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
            "role": "user",
            "groupIds": ["f2bed6b9-6a5a-4363-a9fa-e1f10579c0c1","2368218d-b5ec-4d4d-bc3c-6c249776ee11"]
        }
    }
 */
/**
 * @apiDefine successExampleAdminGetAllGroups
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": [{
            "name" : "test_group_1",
            "imageUrl" : null,
            "groupId" : "6367e722-e857-4d0f-bf78-278a92260418",
            "createdAt" : "2017-12-25T10:56:13.234Z",
            "countUsers" : 3,
            "countTransactions" : 35
        },{
            "name" : "test_group_2",
            "imageUrl" : null,
            "groupId" : "d8gk54a9-f4g8-d2g6-h783-f2ajg83jf5ui",
            "createdAt" : "2017-12-30T18:55:02.678Z",
            "countUsers" : 5,
            "countTransactions" : 11
        }]
    }
 */
/**
 * @apiDefine successExampleAdminGetAllUsers
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": [{
            "username" : "Harry Potter",
            "email" : "harry.potter@hogwarts.edu",
            "userId" : "d9gh1hs7-e8lk-495f-br48-2f4ds92260418",
            "role" : "2017-12-25T10:56:13.234Z",
            "countGroupIds" : 3,
        },{
            "name" : "Ron Weasly",
            "imageUrl" : "ron.weasly@hogwarts.edu",
            "groupId" : "4js8fg66-f4g8-ay98-ql04-f212343jf5ui",
            "createdAt" : "2017-12-20T13:22:02.515Z",
            "countGroupIds" : 5,
        }]
    }
 */
/**
 * @apiDefine successBodyUserUsername
    @apiSuccess (Success) {string} payload[username] Name of user
 */
/**
 * @apiDefine successBodyUserEmail
    @apiSuccess (Success) {string} payload[email] Email of user
 */
/**
 * @apiDefine successBodyUserUserId
    @apiSuccess (Success) {string} payload[userId] Id of user
 */
/**
 * @apiDefine successBodyUserRole
    @apiSuccess (Success) {string} payload[role] Role of user. Supported roles: user, admin
 */
/**
 * @apiDefine successBodyUserGroupIds
    @apiSuccess (Success) {Array[string]} payload[groupIds] Ids of groups the user belongs to
 */
/**
 * @apiDefine successBodyUserCountGroupIds
    @apiSuccess (Success) {int} payload[countGroupIds] Number of groups the user belongs to
 */
/**
 * @apiDefine successBodyGroupName
    @apiSuccess (Success) {string} payload[name] Name of group
 */
/**
 * @apiDefine successBodyGroupImageUrl
    @apiSuccess (Success) {string} payload[imageUrl] ImageUrl of group
 */
/**
 * @apiDefine successBodyGroupId
    @apiSuccess (Success) {string} payload[groupId] Id of group
 */
/**
 * @apiDefine successBodyGroupCreatedAt
    @apiSuccess (Success) {string} payload[createdAt] Date when group was created
 */
/**
 * @apiDefine successBodyGroupCountTransactions
    @apiSuccess (Success) {int} payload[countTransactions] Number of transaction in group
 */
/**
 * @apiDefine successBodyGroupCountUsers
    @apiSuccess (Success) {int} payload[countUsers] Number of users in group
 */
/**
 * @apiDefine successBodyGroupTransactions
    @apiSuccess (Success) {Array[transaction]} payload[transactions] List of transactions in group
 */
/**
 * @apiDefine successBodyGroupUsers
    @apiSuccess (Success) {Array[user]} payload[users] List of users in group
 */
/**
 * @apiDefine successBodyGroupUsersUserId
    @apiSuccess (Success) {string} users[userId] Id of user
 */
/**
 * @apiDefine successBodyGroupUsersName
    @apiSuccess (Success) {string} users[username] Name of user
 */

/**
 * @apiDefine admin Administrator
 *  role:'admin'
 */