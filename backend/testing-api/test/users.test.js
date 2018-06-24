'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const fs = require('fs');

const databaseService = require('../util/databaseService');
const expectResponse = require('../util/expectResponse.util');
const settings = require('../config/settings.config');
const userService = require('../util/userService.util');

chai.use(require('chai-http'));

const userData = require('../data/user.data');

describe('User-Controller', () => {
  /*
  describe('Auth-Type: Facebook', function() {
    before('Clean DB', databaseService.cbResetDB);
    let facebookToken;
    before(function(done) {
      userService.getFacebookTestAccessToken()
        .then((token) => {
          facebookToken = token;
          done();
        }).catch((error) => {
          winston.error('Facbook Login Error', error);
          done();
        });
    });

    // POST - Login/Register new facebook user
    it('Login/Register as facebook user', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=2')
      .send({'accessToken': facebookToken})
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.authType).to.be.equal(2);
        expect(res.body.payload.accessToken).to.have.lengthOf.above(1);
      });
    });

    it('Login/Register as facebook user again', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=2')
      .send({'accessToken': facebookToken})
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.authType).to.be.equal(2);
        expect(res.body.payload.accessToken).to.have.lengthOf.above(1);
      });
    });

    // POST - Send user data as facebook user
    it('Send user data as facebook user', function() {
      return chai.request(settings.host)
      .get(settings.url.test.base + '/authentication/required')
      .set('Authorization', '2 ' + facebookToken)
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
      });
    });

    // POST - Logout as default user
    it('Logout as facebook user', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/logout')
      .set('Authorization', '2 ' + facebookToken)
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
      });
    });

    // POST - Send data with expired token as facebook user
    it('should fail to send data with expired token as facebook user', function() {
      return chai.request(settings.host)
      .get(settings.url.test.base + '/authentication/required')
      .set('Authorization', '2 ' + facebookToken)
      .then(res => {
        expectResponse.toBe401.unknownUserOrExpiredToken(res);
      });
    });

    // POST - Relogin facebook user
    it('Relogin as facebook user', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=2')
      .send({'accessToken': facebookToken})
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
      });
    });

    it('should fail to login with invalid token', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=2')
      .send({'accessToken': 'XXXXX'})
      .then(res => {
        expectResponse.toBe401.loginFailed(res);
      });
    });

    it('should fail to login with empty token', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=2')
      .send({'accessToken': ''})
      .then(res => {
        expectResponse.toBe400.invalidRequestBody(res);
      });
    });

    it('should fail to login with empty body', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=2')
      .send({})
      .then(res => {
        expectResponse.toBe400.invalidRequestBody(res);
      });
    });
  });
  */

  describe('Auth-Type: Password', function() {
    describe('Register', function() {
      before('Clean DB', databaseService.cbResetDB);
      it('should register new user', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.valid[0])
        .then(function(res) {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.accessToken.length).to.equal(200);
          expect(res.body.payload.authType).to.equal(0);
        });
      });

      it('should fail to register existing user', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.valid[0])
        .then(function(res) {
          expectResponse.toBe409.register.userAlreadyExists(res);
        });
      });

      it('should fail to register user with invalid username', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.invalid.invalidUsername)
        .then(function(res) {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to register user with invalid password', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.invalid.invalidPassword)
        .then(function(res) {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to register user with missing username', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.invalid.missingUsername)
        .then(function(res) {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to register user with missing email', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.invalid.missingEmail)
        .then(function(res) {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to register user with missing password', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.invalid.missingPassword)
        .then(function(res) {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to register user with missing imageUrl', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.invalid.missingImageUrl)
        .then(function(res) {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });
    });

    describe('Login', function() {
      before('Clean DB', databaseService.cbResetDB);
      let defaultToken;

      before('register user 1', function(done) {
        userService.register(userData.users.valid[1])
        .then((res) => {
          defaultToken = res.body.payload.accessToken;
          done();
        });
      });

      it('should login as registered user', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({username: userData.users.valid[1].username,
          password: userData.users.valid[1].password})
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.authType).to.equal(0);
          expect(res.body.payload.accessToken).to.have.lengthOf(200);
        });
      });

      it('should fail to login with invalid password', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({username: userData.users.valid[1].username,
          password: 'XXXXX'})
        .then(res => {
          expectResponse.toBe401.loginFailed(res);
        });
      });

      it('should fail to login with empty password', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({username: userData.users.valid[1].username,
          password: ''})
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to login with no password', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({username: userData.users.valid[1].username})
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to login with no username', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({password: userData.users.valid[2].password})
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to login with unknown username', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({username: 'unknownUsername',
          password: 'passwordX'})
        .then(res => {
          expectResponse.toBe401.loginFailed(res);
        });
      });

      it('should fail to login with empty body', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({})
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

    });

    describe('Logout', function() {
      before('Clean DB', databaseService.cbResetDB);
      let defaultToken;

      before(function(done) {
        chai.request(settings.host)
        .post(settings.url.users.base + '/')
        .send(userData.users.valid[4])
        .then((res) => {
          defaultToken = res.body.payload.accessToken;
          done();
        });
      });

      // POST - Logout as default user
      it('should logout', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/logout')
        .set('Authorization', '0 ' + defaultToken)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
        });
      });

      // POST - Send data with expired token as default user
      it('should fail to send data with expired token as default user', function() {
        return chai.request(settings.host)
        .get(settings.url.test.base + '/authentication/required')
        .set('Authorization', '0 ' + defaultToken)
        .then(res => {
          expectResponse.toBe401.unknownUserOrExpiredToken(res);
        });
      });

      // POST - Relogin as default user
      it('should re-login', function() {
        return chai.request(settings.host)
        .post(settings.url.users.base + '/login?type=0')
        .send({username: userData.users.valid[4].username,
          password: userData.users.valid[4].password})
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
        });
      });
    });
  });

  describe('Invalid Auth-Type', function() {
    before('Clean DB', databaseService.cbResetDB);
    it('should fail with an invalid auth type', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login?type=99')
      .send({username: userData.users.valid[1].username,
        password: userData.users.valid[1].password})
      .then(res => {
        expectResponse.toBe401.invalidAuthType(res);
      });
    });

    it('should fail with no auth type', function() {
      return chai.request(settings.host)
      .post(settings.url.users.base + '/login')
      .send({username: userData.users.valid[1].username,
        password: userData.users.valid[1].password})
      .then(res => {
        expectResponse.toBe401.invalidAuthType(res);
      });
    });
  });

  describe('Get User', function() {
    let tokens = {};
    let facebookToken;
    before('Clean DB and register User 0 and 1', done => {
      databaseService.promiseResetDB().then(()=> {
        return chai.request(settings.host).post(settings.url.users.base  + '/').send(userData.users.valid[0]);
      }).then(res => {
        tokens[0] = res.body.payload.accessToken;
        return chai.request(settings.host).post(settings.url.users.base  + '/').send(userData.users.valid[1]);
      }).then(res => {
        tokens[1] = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        winston.error('Register User Error:', error);
      });
    });

    before('get test facebook access token', function(done) {
      userService.getFacebookTestAccessToken()
        .then((token) => {
          facebookToken = token;
          done();
        }).catch((error) => {
          winston.error('failed to get test facebook access token', error);
          done();
        });
    });

    before('Login as facebook user', function(done) {
      userService.loginFacebook({'accessToken': facebookToken})
      .then(res => {done();})
      .catch((error) => {winston.error('Facbook Login Error', error);});
    });
    /*
    it('should get the user-data of facebook_user', function() {
      return chai.request(settings.host)
      .get(settings.url.users.base  + '/user')
      .set('Authorization', '2 ' + facebookToken)
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.username).to.equal('Tom Albcdbiefbgfd Moiduman');
        expect(res.body.payload.email).to.equal('ictevhhpns_1513004432@tfbnw.net');
        expect(res.body.payload._id).to.be.undefined;
        expect(res.body.payload.groupIds).to.be.undefined;
        expect(res.body.payload.userId).to.equal('110340223090296');
        expect(res.body.payload.role).to.equal('user');
        // expect(res.body.payload.imageUrl).to.equal('https://scontent.xx.fbcdn.net/v/t1.0-1/c15.0.50.50' +
        // '/p50x50/10354686_10150004552801856_220367501106153455_n.jpg?oh=baf3745408876788393e9ca2b7e1dc94&oe=5AEBF02F');
        expect(res.body.payload.imageUrl).to.equal('https://graph.facebook.com/v2.11/110340223090296' +
        '/picture?type=normal');
      });
    });
    */
    it('should get the user-data of user_0', function() {
      return chai.request(settings.host)
      .get(settings.url.users.base  + '/user')
      .set('Authorization', '0 ' + tokens[0])
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.username).to.equal(userData.users.valid[0].username);
        expect(res.body.payload.email).to.equal(userData.users.valid[0].email);
        expect(res.body.payload._id).to.be.undefined;
        expect(res.body.payload.groupIds).to.be.undefined;
        expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
        expect(res.body.payload.role).to.equal('user');
        expect(res.body.payload.imageUrl).to.equal(userData.users.valid[0].imageUrl);
      });
    });

    it('should get the user-data of user_1', function() {
      return chai.request(settings.host)
      .get(settings.url.users.base  + '/user')
      .set('Authorization', '0 ' + tokens[1])
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.username).to.equal(userData.users.valid[1].username);
        expect(res.body.payload.email).to.equal(userData.users.valid[1].email);
        expect(res.body.payload._id).to.be.undefined;
        expect(res.body.payload.groupIds).to.be.undefined;
        expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
        expect(res.body.payload.role).to.equal('user');
        expect(res.body.payload.imageUrl).to.be.null;
      });
    });

    it('should not get the user-data of user_0 due to wrong token', function() {
      return chai.request(settings.host)
      .get(settings.url.users.base  + '/user')
      .set('Authorization', '0 this_is_a_wrong_token')
      .then(res => {
        expectResponse.toBe401.invalidAuthToken(res);
      });
    });

    it('should not get the user-data of user_0 due to missing auth header', function() {
      return chai.request(settings.host)
      .get(settings.url.users.base  + '/user')
      .then(res => {
        expectResponse.toBe401.missingHeaderAuthorization(res);
      });
    });
  });

  describe('Update user', () => {
    describe('all attributes', function() {
      let token;
      let constantUserData = {};

      before('Clean DB and register User 0', done => {
        databaseService.promiseResetDB().then(()=> {
          return userService.register(userData.users.valid[0]);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          winston.error('Register User Error:', error);
        });
      });

      it('should get the original user data of user_0', function() {
        return chai.request(settings.host)
        .get(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(userData.users.valid[0].username);
          expect(res.body.payload.email).to.equal(userData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.be.undefined;
          expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(userData.users.valid[0].imageUrl);
          constantUserData.groupIds = res.body.payload.groupIds;
          constantUserData.userId = res.body.payload.userId;
        });
      });

      it('should update user_0', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.valid.allFields)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
        });
      });

      it('should get the updated user data of user_0', function() {
        return chai.request(settings.host)
        .get(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(userData.users.update.valid.allFields.username);
          expect(res.body.payload.email).to.equal(userData.users.update.valid.allFields.email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.equal(constantUserData.groupIds);
          expect(res.body.payload.userId).to.equal(constantUserData.userId);
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(userData.users.update.valid.allFields.imageUrl);
        });
      });
    });

    describe('one attribute', function() {
      let token;
      let constantUserData = {};

      before('Clean DB and register User 0', done => {
        databaseService.promiseResetDB().then(()=> {
          return userService.register(userData.users.valid[0]);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          winston.error('Register User Error:', error);
        });
      });

      it('should get the original user data of user_0', function() {
        return chai.request(settings.host)
        .get(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(userData.users.valid[0].username);
          expect(res.body.payload.email).to.equal(userData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.be.undefined;
          expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(userData.users.valid[0].imageUrl);
          constantUserData.groupIds = res.body.payload.groupIds;
          constantUserData.userId = res.body.payload.userId;
        });
      });

      it('should update user_0', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.valid.oneFieldUsername)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
        });
      });

      it('should get the updated user data of user_0', function() {
        return chai.request(settings.host)
        .get(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(userData.users.update.valid.oneFieldUsername.username);
          expect(res.body.payload.email).to.equal(userData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.equal(constantUserData.groupIds);
          expect(res.body.payload.userId).to.equal(constantUserData.userId);
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(userData.users.valid[0].imageUrl);
        });
      });
    });

    describe('with error', function() {
      let token;

      before('Clean DB and register User 0', done => {
        databaseService.promiseResetDB().then(()=> {
          return userService.register(userData.users.valid[0]);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          winston.error('Register User Error:', error);
        });
      });

      it('should fail to update due to missing payload', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update userId', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateUserId)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update groupd ids', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateGroupIds)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update internal id', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateInternalId)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update authType', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateAuthType)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update role', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateRole)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update unknown field', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateUnknownField)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update username with null', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateUsernameNull)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update password with null', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updatePasswordNull)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to update email with null', function() {
        return chai.request(settings.host)
        .put(settings.url.users.base  + '/user')
        .set('Authorization', '0 ' + token)
        .send(userData.users.update.invalid.updateEmailNull)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });
    });
  });
});
