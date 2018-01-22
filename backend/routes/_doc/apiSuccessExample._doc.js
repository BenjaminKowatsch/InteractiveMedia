'use strict';

// jscs:disable jsDoc

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
 * @apiDefine successExampleCreateGroup
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "name" : "test_group_1",
            "imageUrl" : null,
            "users" : [{
                "userId": "f2bed6b9-6a5a-4363-a9fa-e1f10579c0c1",
                "username": "user_1_name",
                "email": "user_1_email",
                "imageUrl": "http://example.com/image.png"
            },{
                "userId": "2368218d-b5ec-4d4d-bc3c-6c249776ee11",
                "username": "user_2_name",
                "email": "user_2_email"
                "imageUrl": null
            }]
            "transactions" : [ ... ], // all transaction-objects, length=0 if group was just created
            "groupId" : "6367e722-e857-4d0f-bf78-278a92260418",
            "createdAt" : "2017-12-25T10:56:13.234Z"
        }
    }
 */

/**
 * @apiDefine successExampleGetGroupById
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "name" : "test_group_1",
            "imageUrl" : null,
            "users" : [{
                "userId": "f2bed6b9-6a5a-4363-a9fa-e1f10579c0c1",
                "username": "user_1_name",
                "email": "user_1_email",
                "imageUrl": "http://example.com/image.png"
            },{
                "userId": "2368218d-b5ec-4d4d-bc3c-6c249776ee11",
                "username": "user_2_name",
                "email": "user_2_email",
                "imageUrl": null
            }]
            "transactions" : [ ... ], // all transaction-objects, length=0 if group was just created
            "groupId" : "6367e722-e857-4d0f-bf78-278a92260418",
            "createdAt" : "2017-12-25T10:56:13.234Z"
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
 * @apiDefine successExampleUser
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "username": "my_user_name",
            "email": "my_user_mail@example.com",
            "userId": "6367e722-e857-4d0f-bf78-278a92260418",
            "role": "user",
            "groupIds": ["f2bed6b9-6a5a-4363-a9fa-e1f10579c0c1","2368218d-b5ec-4d4d-bc3c-6c249776ee11"],
            "imageUrl": "http://exmaple.com/image.png"
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
    @apiSuccessExample {JSON} Success-Response
    {
        "success": true,
        "payload": [{
            "username" : "Harry Potter",
            "email" : "harry.potter@hogwarts.edu",
            "userId" : "d9gh1hs7-e8lk-495f-br48-2f4ds92260418",
            "role" : "user",
            "authType" : 0,
            "countGroupIds" : 3,
        },{
            "username" : "Ron Weasly",
            "email" : "ron.weasly@hogwarts.edu",
            "userId" : "4js8fg66-f4g8-ay98-ql04-f212343jf5ui",
            "role" : "user",
            "authType" : 0,
            "countGroupIds" : 5,
        }]
    }
 */

/**
 * @apiDefine successExampleTransaction
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": {
            "publishedAt" : "2017-04-23T19:34:23.321Z",
            "amount": 1234.13,
            "infoName": "A very expensive Beer",
            "infoLocation": {
                "latitude": 48.947,
                "longitude": 9.131
            },
            "infoCreatedAt": "2017-04-23T18:25:43.511Z",
            "infoImageUrl": "a97c6b8e08f9d7a.image.jpg",
            "paidBy": "6367e722-e857-4d0f-bf78-278a92260418",
            "split": "even"
        }
    }
 */

/**
 * @apiDefine successExampleTransactions
    @apiSuccessExample {type} Success-Response
    {
        "success": true,
        "payload": [{
            "publishedAt" : "2017-04-23T19:34:23.321Z",
            "amount": 1234.13,
            "infoName": "A very expensive Beer",
            "infoLocation": {
                "latitude": 48.947,
                "longitude": 9.131
            },
            "infoCreatedAt": "2017-04-23T18:25:43.511Z",
            "infoImageUrl": "a97c6b8e08f9d7a.image.jpg",
            "paidBy": "6367e722-e857-4d0f-bf78-278a92260418",
            "split": "even"
        },
        {
            "amount": 9999.99,
            "infoName": "A unicorn for Simon",
            "infoLocation": {
                "latitude": null,
                "longitude": null
            },
            "infoCreatedAt": "2017-07-17T17:17:17.017Z",
            "infoImageUrl": null,
            "paidBy": "d8gk54a9-f4g8-d2g6-h783-f2ajg83jf5ui",
            "split": "even"
        }]
    }
*/
