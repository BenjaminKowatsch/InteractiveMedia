'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  REGISTER_USER: '/v1/users',
  BASE_USER: '/v1/users',
  TEST_NOTIFICATION: '/v1/test/notification'
};

const userData = require('./data/user.data');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.REGISTER_USER).send(userData.users.valid[index]);

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
      chai.request(HOST)
      .put(URL.BASE_USER  + '/user/fcmtoken')
      .set('Authorization', '0 ' + token)
      .send({fcmToken: fcmToken})
      .then(res => {
        done();
      });
    });

    it('should send notification via fcm to registered user', function() {
      return chai.request(HOST)
          .post(URL.TEST_NOTIFICATION + '/user')
          .set('Authorization', '0 ' + token)
          .send({dryRun: true})
          .then(res => {
            expect(res).to.have.status(200);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
          });
    });

    it('should fail to send notification with no authorization header', function() {
      return chai.request(HOST)
          .post(URL.TEST_NOTIFICATION + '/user')
          .send({dryRun: true})
          .then(res => {
            expect(res).to.have.status(401);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.be.equal('authentication');
            expect(res.body.payload.message).to.be.equal('no http request header Authorization provided');
          });
    });
  });
