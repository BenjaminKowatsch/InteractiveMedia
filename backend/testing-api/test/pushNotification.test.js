'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseService = require('../util/databaseService');
const settings = require('../config/settings.config');
const userService = require('../util/userService.util');

chai.use(require('chai-http'));

const userData = require('../data/user.data');

// skip this test because Firebase Cloud Messaging does not provide a test token which is valid and does not expire
// hence if you use a genuine token it will expire after a couple of hours (presumably 48 hours)
// nevertheless you can use this test by pasting a valid fcmToken
describe.skip('PushNotifications', () => {
    let token;
    let fcmToken = 'cUKrLfXhKSU:APA91bFfh3KX44JYptYsslEVtsAv4VwppWlDXJofsaEiwRiMF48' +
      '_OtBftNydOnDnV4WSEVqqz-3D2twflIFRIePuiTr_2Tn75ZSZRbgOnLeY_lCkkHvvjZ6i-GzJxvkdtf-n0no701Do';

    before('register User 0', done => {
      databaseService.promiseResetDB().then(()=> {
        return userService.register(userData.users.valid[0]);
      }).then(res => {
        token = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        winston.error('Register User Error:', error);
      });
    });

    before('set fcm token of user_0', done => {
      chai.request(settings.host)
      .put(settings.url.users.base  + '/user')
      .set('Authorization', '0 ' + token)
      .send({fcmToken: fcmToken})
      .then(res => {
        done();
      });
    });

    it('should send notification via fcm to registered user', function() {
      return chai.request(settings.host)
          .post(settings.url.test.notification + '/user')
          .set('Authorization', '0 ' + token)
          .send({dryRun: true})
          .then(res => {
            expect(res).to.have.status(200);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
          });
    });
  });
