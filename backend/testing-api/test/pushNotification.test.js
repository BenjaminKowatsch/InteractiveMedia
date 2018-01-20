'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

const userData = require('./data/user.data');

// ************* Helper ***********//

const registerUser = index => chai.request(settings.host).post(settings.url.users.register)
.send(userData.users.valid[index]);

describe.skip('PushNotifications', () => {
    let token;
    let fcmToken = 'cUKrLfXhKSU:APA91bFfh3KX44JYptYsslEVtsAv4VwppWlDXJofsaEiwRiMF48' +
      '_OtBftNydOnDnV4WSEVqqz-3D2twflIFRIePuiTr_2Tn75ZSZRbgOnLeY_lCkkHvvjZ6i-GzJxvkdtf-n0no701Do';

    before('register User 0', done => {
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
        token = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
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
