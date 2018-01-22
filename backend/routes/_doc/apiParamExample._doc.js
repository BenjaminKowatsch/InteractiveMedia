'use strict';

// jscs:disable jsDoc

/**
 * @apiDefine paramExampleRegister
    @apiParamExample {type} Register User
    {
        "username" : "alex",
        "password" : "XHDGETFHFJCHF",
        "email" : "alex@example.com"
        "imageUrl" : "http://example.com/image.png"
    }
    @apiParamExample {type} Register User without optional values
    {
        "username" : "alex",
        "password" : "XHDGETFHFJCHF",
        "email" : "alex@example.com"
        "imageUrl" : null
    }
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
        "accessToken" : "DeUBZBpAr9KCBZB3knwe1eGvcur"
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
 * @apiDefine paramExampleCreateGroup
    @apiParamExample {type} CreateGroup
    {
        name: 'TheFooBars',
        imageUrl: null,
        users: ['user_1@example.de', 'user_2@example.de', 'user_3@example.de']
    }
 */
/**
 * @apiDefine paramExampleUserUpdate
    @apiParamExample {JSON} Update all attributes
    {
        "username": "new user name",
        "email": "new.alex@alex.de",
        "password": "hashedPassword",
        "imageUrl": "http://new.example.com/image.jpg",
        "fcmToken": "cUf35139J8U:APA91bH6pkjWHRAUAW52QGQV6tR8SQdbpJK20QitJrAyWfX22VP4G0OUL-cwnXQob507qnBILDkZaoY0IW3eAvAevjM5dgCTbL297n1pbXoEHLzNDKV-86xJkle0TR6RBi8fA3BzEEOr"
    }
    @apiParamExample {JSON} Update at least one attribute
    {
        "fcmToken": "cUf35139J8U:APA91bH6pkjWHRAUAW52QGQV6tR8SQdbpJK20QitJrAyWfX22VP4G0OUL-cwnXQob507qnBILDkZaoY0IW3eAvAevjM5dgCTbL297n1pbXoEHLzNDKV-86xJkle0TR6RBi8fA3BzEEOr"
    }
 */
/**
 * @apiDefine paramExampleAdminUserUpdate
    @apiParamExample {JSON} Update all attributes
    {
        "username": "new user name",
        "email": "new.alex@alex.de",
        "password": "hashedPassword",
        "imageUrl": "http://new.example.com/image.jpg",
        "fcmToken": "cUf35139J8U:APA91bH6pkjWHRAUAW52QGQV6tR8SQdbpJK20QitJrAyWfX22VP4G0OUL-cwnXQob507qnBILDkZaoY0IW3eAvAevjM5dgCTbL297n1pbXoEHLzNDKV-86xJkle0TR6RBi8fA3BzEEOr",
        "role": "user"
    }
    @apiParamExample {JSON} Update at least one attribute
    {
        "fcmToken": "cUf35139J8U:APA91bH6pkjWHRAUAW52QGQV6tR8SQdbpJK20QitJrAyWfX22VP4G0OUL-cwnXQob507qnBILDkZaoY0IW3eAvAevjM5dgCTbL297n1pbXoEHLzNDKV-86xJkle0TR6RBi8fA3BzEEOr"
    }
 */
/**
 * @apiDefine paramExampleCreateTransaction
    @apiParamExample {type} Create Transaction
    {
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
    @apiParamExample {type} Create Transaction without optional values
    {
        "amount": 1234.13,
        "infoName": "A very expensive Beer",
        "infoLocation": {
            "latitude": null,
            "longitude": null
        },
        "infoCreatedAt": "2017-04-23T18:25:43.511Z",
        "infoImageUrl": null,
        "paidBy": "6367e722-e857-4d0f-bf78-278a92260418",
        "split": "even"
    }
*/
