'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

const userData = require('./data/user.data');

const MINUTE = 60000;
const nowPlus = time => new Date(new Date().getTime() + time).toISOString();

// ************* Helper ***********//

const registerUser = index => chai.request(settings.host).post(settings.url.users.register)
.send(userData.users.valid[index]);

describe.skip('PushNotifications create transactions', function() {
    let tokens = {};
    let userIds = {};
    let groupId;
    const fcmToken = 'eHzuPFGqFS0:APA91bGGI7Egud01UYJdvsZQbzY1JzHSCuyUibTy-yNOJimY7YOQuZHZ1jnSUv6Z' +
    '_dSrALygUJ4RFzurwrGvaIE8o2W33SX1L7R0bGU6A1-ARZCu2kL6Pgk2hF59B8BR-OIVPIzVi0aS';

    before('register user 0 and 1', function(done) {
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
        tokens[0] = res.body.payload.accessToken;
        return registerUser(1);
      }).then(res => {
        tokens[1] = res.body.payload.accessToken;
        done();
      }).catch((error) => {console.log('Register User Error: ' + error);});
    });

    before('create group', function(done) {
      chai.request(settings.host)
        .post(settings.url.groups.base  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send({
          name: 'test_gruppe_1',
          imageUrl: null,
          users: [userData.users.valid[0].email, userData.users.valid[1].email]
        })
      .then(res => {
        groupId = res.body.payload.groupId;
        userIds[0] = res.body.payload.users[0].userId;
        userIds[1] = res.body.payload.users[1].userId;
        done();
      })
      .catch(error => {
        console.log('Create group error: ' + error);
      });
    });

    before('set fcm token of user_0', function(done) {
      chai.request(settings.host)
      .put(settings.url.users.base  + '/user')
      .set('Authorization', '0 ' + tokens[0])
      .send({fcmToken: fcmToken})
      .then(res => {done();})
      .catch((error) => {console.log('Set fcm token error: ' + error);});
    });

    it('create transaction with user_1', function() {
      return chai.request(settings.host)
        .post(settings.url.groups.base + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + tokens[1])
        .send({
          amount: 9,
          infoName: 'Test transaction 0',
          infoLocation: {
            longitude: 9.131,
            latitude: 48.947
          },
          infoCreatedAt: nowPlus(1 * MINUTE),
          infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
          paidBy: userIds[1],
          split: 'even'
        })
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
        });
    });
  });
