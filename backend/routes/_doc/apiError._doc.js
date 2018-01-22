'use strict';

// jscs:disable jsDoc

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
 * @apiDefine error400InvalidBody
    @apiError (ErrorCode) {400} InvalidBody Validation error for request body
 */
/**
 * @apiDefine error400InvalidSplit
    @apiError (ErrorCode) {400} InvalidSplit Invalid splits in request body
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
 * @apiDefine error400UserNotInGroup
    @apiError (ErrorCode) {400} UserNotInGroup A given userId is not part of the given group
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
 * @apiDefine error404UnknownId
    @apiError (ErrorCode) {404} UnknownId Id for the requested resource is unknown
 */
/**
 * @apiDefine error404ResourceNotFound
    @apiError (ErrorCode) {404} NotFound Requested resource is not available
 */

/**
 * @apiDefine error409ConflictUserAlreadyExists
    @apiError (ErrorCode) {409} Conflict User already exists
 */
/**
 * @apiDefine error409UnknownUser
    @apiError (ErrorCode) {409} UnknownUser Unknown user or userId: {{unknwon_mail@mail.com || unknwonUserId}}
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
 * @apiDefine error500UnknownUser
    @apiError (ErrorCode) {500} UnknownUser Requestd group or user references a non-existing userId
 */
