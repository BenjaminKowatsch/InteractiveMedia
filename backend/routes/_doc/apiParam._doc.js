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
 * @apiDefine paramUsernameOptional
    @apiParam (Parameter) {string} username Username. Optional.
 */
/**
 * @apiDefine paramHashedPassword
    @apiParam (Parameter) {string} password Hash of a password
 */
/**
 * @apiDefine paramHashedPasswordOptional
    @apiParam (Parameter) {string} password Hash of a password. Optional.
 */
/**
 * @apiDefine paramAuthtype
    @apiParam (Parameter) {int} authType Login type with authType=(0: Password, 1:Google, 2: Facebook)
 */
/**
 * @apiDefine paramEmail
    @apiParam (Parameter) {string} email Email of user
 */
/**
 * @apiDefine paramEmailOptional
    @apiParam (Parameter) {string} email Email of user. Optional.
 */
/**
 * @apiDefine paramFcmTokenOptional
    @apiParam (Parameter) {string} fcmToken Firebase cloud messaging token of user. Optional. "null" is allowed.
 */
/**
 * @apiDefine paramRoleOptional
    @apiParam (Parameter) {string} role Role of user. Optional. "user" and "admin" is allowed.
 */
/**
 * @apiDefine paramImageUrlRequiredButNull
    @apiParam (Parameter) {string} imageUrl Url of user's profile image. "null" is allowed.
 */
/**
 * @apiDefine paramImageUrlOptional
    @apiParam (Parameter) {string} imageUrl Url of user's profile image. Optional. "null" is allowed.
 */
/**
 * @apiDefine paramUrlGroupId
    @apiParam (URL-Parameter) {string} groupId Id of the requested group
 */
/**
 * @apiDefine paramUrlTransactionsAfterDate
    @apiParam (URL-Parameter) {string} after ISO-8601-Date to get all transactions after the date, format format: YYYY-MM-DDTHH:mm:ss.sssZ
 */
/**
 * @apiDefine paramUrlUserId
    @apiParam (URL-Parameter) {string} userId Id of the requested user
 */
/**
 * @apiDefine paramTransactionObject
    @apiParam (Parameter) {Number} amount Amout of the transaction
    @apiParam (Parameter) {string} infoName Name or reason for the transaction
    @apiParam (Parameter) {Number} infoLocation[latitude] Geoinformation: Latitude, can be null
    @apiParam (Parameter) {Number} infoLocation[longitude] Geoinformation: Longitude, can be null
    @apiParam (Parameter) {string} infoCreatedAt Date when the transaction was created in the app (ISO-8601, format: YYYY-MM-DDTHH:mm:ss.sssZ)
    @apiParam (Parameter) {string} infoImageUrl URL of the transaction-image, can be null
    @apiParam (Parameter) {string} paidBy Id of user who payed the expense
    @apiParam (Parameter) {string} split Methode to split, currently supported: [even-spilt: value="even"]
 */
