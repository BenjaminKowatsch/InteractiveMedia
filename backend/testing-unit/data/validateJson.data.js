'use strict';

module.exports = {
  updateUserAsAdminPayload: {
    valid: {
      allFields: {
        username: 'new user name',
        password: 'hashedPassword',
        email: 'new.alex@alex.de',
        imageUrl: 'http://new.example.com/image.jpg',
        fcmToken: 'cUf35139J8U:APA91bH6pkjWHRAUAW52QGQV6tR8SQdbpJK20QitJrAyWfX22VP4G0OUL-' +
          'cwnXQob507qnBILDkZaoY0IW3eAvAevjM5dgCTbL297n1pbXoEHLzNDKV-86xJkle0TR6RBi8fA3BzEEOr',
        role: 'admin'
      },
      oneFieldUsername: {
        username: 'Harry Hirsch'
      }
    },
    invalid: {
      updateUserId: {
        userId: 'AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA'
      },
      updateGroupIds: {
        groupIds: ['BBBBBBBB-BBBB-BBBB-BBBB-BBBBBBBBBBBB']
      },
      updateInternalId: {
        _id: 'aaaaaaaaaaaaaaaaaaaaaaaa'
      },
      updateAuthType: {
        authType: 1
      },
      updateUnknownField: {
        unknownField: 'value of unknown filed'
      },
      updateInvalidRole: {
        role: 'XXXX'
      },
      updateUsernameNull: {
        username: null
      },
      updatePasswordNull: {
        password: null
      },
      updateEmailNull: {
        email: null
      }
    }
  }
};
