'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const fs = require('fs');
const expect = require('chai').expect;
const databaseHelper = require('./data/databaseHelper');
const expectResponse = require('../util/expectResponse.util');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  BASE_USER: '/v1/users',
  BASE_TEST: '/v1/test'
};

const https = require('https');
const config = {
  'facebookUrlAppToken': process.env.FACEBOOK_URL_APP_TOKEN,
  'facebookAppId': process.env.FACEBOOK_APP_ID
};

const testData = require('./data/user.data');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.BASE_USER).send(testData.users.valid[index]);

function getFacebookTestAccessToken() {
  return new Promise((resolve, reject) => {
    https.get('https://graph.facebook.com/v2.11/' + config.facebookAppId + '/' +
    'accounts/test-users?access_token=' +
    config.facebookUrlAppToken, function(response) {
      let responseMessage = '';

      response.on('data', function(chunk) {
        responseMessage += chunk;
      });

      response.on('end', function() {
        const data = JSON.parse(responseMessage);
        if (data.length <= 0) {
          reject(data);
        } else {
          resolve(data.data[0].access_token);
        }
      });
    });
  });
}

describe('User-Controller', () => {

  describe('Auth-Type: Facebook', function() {
    before('Clean DB', databaseHelper.cbResetDB);
    let facebookToken;
    before(function(done) {
      getFacebookTestAccessToken()
        .then((token) => {
          facebookToken = token;
          done();
        }).catch((error) => {
          console.log('Facbook Login Error: ' + error);
          done();
        });
    });

    // POST - Login/Register new facebook user
    it('Login/Register as facebook user', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
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
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
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
      return chai.request(HOST)
      .get(URL.BASE_TEST + '/authentication/required')
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
      return chai.request(HOST)
      .post(URL.BASE_USER + '/logout')
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
      return chai.request(HOST)
      .get(URL.BASE_TEST + '/authentication/required')
      .set('Authorization', '2 ' + facebookToken)
      .then(res => {
        expect(res).to.have.status(401);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
      });
    });

    // POST - Relogin facebook user
    it('Relogin as facebook user', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
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
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
      .send({'accessToken': 'XXXXX'})
      .then(res => {
        expect(res).to.have.status(401);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload.dataPath).to.equal('login');
        expect(res.body.payload.message).to.equal('login failed');
      });
    });

    it('should fail to login with empty token', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
      .send({'accessToken': ''})
      .then(res => {
        expectResponse.toBe400InvalidRequestBody(res);
      });
    });

    it('should fail to login with empty body', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
      .send({})
      .then(res => {
        expectResponse.toBe400InvalidRequestBody(res);
      });
    });
  });

  describe('Auth-Type: Password', function() {
    describe('Register', function() {
      before('Clean DB', databaseHelper.cbResetDB);
      it('should register new user', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.valid[0])
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
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.valid[0])
        .then(function(res) {
          expect(res).to.have.status(409);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('register');
          expect(res.body.payload.message).to.equal('username already exists');
        });
      });

      it('should fail to register user with invalid username', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.invalid.invalidUsername)
        .then(function(res) {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to register user with invalid password', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.invalid.invalidPassword)
        .then(function(res) {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to register user with missing username', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.invalid.missingUsername)
        .then(function(res) {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to register user with missing email', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.invalid.missingEmail)
        .then(function(res) {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to register user with missing password', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.invalid.missingPassword)
        .then(function(res) {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to register user with missing imageUrl', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.invalid.missingImageUrl)
        .then(function(res) {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });
    });

    describe('Login', function() {
      before('Clean DB', databaseHelper.cbResetDB);
      let defaultToken;

      before('register user 1', function(done) {
        registerUser(1)
        .then((res) => {
          defaultToken = res.body.payload.accessToken;
          done();
        });
      });

      it('should login as registered user', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[1].username,
          password: testData.users.valid[1].password})
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
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[1].username,
          password: 'XXXXX'})
        .then(res => {
          expect(res).to.have.status(401);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('login');
          expect(res.body.payload.message).to.equal('login failed');
        });
      });

      it('should fail to login with empty password', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[1].username,
          password: ''})
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to login with no password', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[1].username})
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to login with no username', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({password: testData.users.valid[2].password})
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to login with unknown username', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: 'unknownUsername',
          password: 'passwordX'})
        .then(res => {
          expect(res).to.have.status(401);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('login');
          expect(res.body.payload.message).to.equal('login failed');
        });
      });

      it('should fail to login with empty body', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({})
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

    });

    describe('Logout', function() {
      before('Clean DB', databaseHelper.cbResetDB);
      let defaultToken;

      before(function(done) {
        chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send(testData.users.valid[4])
        .then((res) => {
          defaultToken = res.body.payload.accessToken;
          done();
        });
      });

      // POST - Logout as default user
      it('should logout', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/logout')
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
        return chai.request(HOST)
        .get(URL.BASE_TEST + '/authentication/required')
        .set('Authorization', '0 ' + defaultToken)
        .then(res => {
          expect(res).to.have.status(401);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload.dataPath).to.equal('user');
          expect(res.body.payload.message).to.equal('unknown user or expired token');
        });
      });

      // POST - Relogin as default user
      it('should re-login', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[4].username,
          password: testData.users.valid[4].password})
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
    before('Clean DB', databaseHelper.cbResetDB);
    it('should fail with an invalid auth type', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=99')
      .send({username: testData.users.valid[1].username,
        password: testData.users.valid[1].password})
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('authType');
        expect(res.body.payload.message).to.equal('invalid auth type');
      });
    });

    it('should fail with no auth type', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login')
      .send({username: testData.users.valid[1].username,
        password: testData.users.valid[1].password})
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('authType');
        expect(res.body.payload.message).to.equal('invalid auth type');
      });
    });
  });

  describe('Get User', function() {
    let tokens = {};
    let facebookToken;
    before('Clean DB and register User 0 and 1', done => {
      databaseHelper.promiseResetDB().then(()=> {
        return chai.request(HOST).post(URL.BASE_USER  + '/').send(testData.users.valid[0]);
      }).then(res => {
        tokens[0] = res.body.payload.accessToken;
        return chai.request(HOST).post(URL.BASE_USER  + '/').send(testData.users.valid[1]);
      }).then(res => {
        tokens[1] = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    before('get test facebook access token', function(done) {
      getFacebookTestAccessToken()
        .then((token) => {
          facebookToken = token;
          done();
        }).catch((error) => {
          console.log('failed to get test facebook access token: ' + error);
          done();
        });
    });

    before('Login as facebook user', function(done) {
      chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
      .send({'accessToken': facebookToken})
      .then(res => {done();})
      .catch((error) => {console.log('Facbook Login Error: ' + error);});
    });

    it('should get the user-data of facebook_user', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER  + '/user')
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
        expect(res.body.payload.imageUrl).to.equal('https://scontent.xx.fbcdn.net/v/t1.0-1/c15.0.50.50' +
        '/p50x50/10354686_10150004552801856_220367501106153455_n.jpg?oh=baf3745408876788393e9ca2b7e1dc94&oe=5AEBF02F');
      });
    });

    it('should get the user-data of user_0', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER  + '/user')
      .set('Authorization', '0 ' + tokens[0])
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.username).to.equal(testData.users.valid[0].username);
        expect(res.body.payload.email).to.equal(testData.users.valid[0].email);
        expect(res.body.payload._id).to.be.undefined;
        expect(res.body.payload.groupIds).to.be.undefined;
        expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
        expect(res.body.payload.role).to.equal('user');
        expect(res.body.payload.imageUrl).to.equal(testData.users.valid[0].imageUrl);
      });
    });

    it('should get the user-data of user_1', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER  + '/user')
      .set('Authorization', '0 ' + tokens[1])
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.username).to.equal(testData.users.valid[1].username);
        expect(res.body.payload.email).to.equal(testData.users.valid[1].email);
        expect(res.body.payload._id).to.be.undefined;
        expect(res.body.payload.groupIds).to.be.undefined;
        expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
        expect(res.body.payload.role).to.equal('user');
        expect(res.body.payload.imageUrl).to.be.null;
      });
    });

    it('should not get the user-data of user_0 due to wrong token', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER  + '/user')
      .set('Authorization', '0 this_is_a_wrong_token')
      .then(res => {
        expectResponse.toBe401InvalidAuthToken(res);
      });
    });

    it('should not get the user-data of user_0 due to missing auth header', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER  + '/user')
      .then(res => {
        expect(res).to.have.status(401);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('authentication');
        expect(res.body.payload.message).to.equal('no http request header Authorization provided');
      });
    });
  });

  describe('Update user', () => {
    describe('all attributes', function() {
      let token;
      let constantUserData = {};

      before('Clean DB and register User 0', done => {
        databaseHelper.promiseResetDB().then(()=> {
          return registerUser(0);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      it('should get the original user data of user_0', function() {
        return chai.request(HOST)
        .get(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(testData.users.valid[0].username);
          expect(res.body.payload.email).to.equal(testData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.be.undefined;
          expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(testData.users.valid[0].imageUrl);
          constantUserData.groupIds = res.body.payload.groupIds;
          constantUserData.userId = res.body.payload.userId;
        });
      });

      it('should update user_0', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.valid.allFields)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
        });
      });

      it('should get the updated user data of user_0', function() {
        return chai.request(HOST)
        .get(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(testData.users.update.valid.allFields.username);
          expect(res.body.payload.email).to.equal(testData.users.update.valid.allFields.email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.equal(constantUserData.groupIds);
          expect(res.body.payload.userId).to.equal(constantUserData.userId);
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(testData.users.update.valid.allFields.imageUrl);
        });
      });
    });

    describe('one attribute', function() {
      let token;
      let constantUserData = {};

      before('Clean DB and register User 0', done => {
        databaseHelper.promiseResetDB().then(()=> {
          return registerUser(0);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      it('should get the original user data of user_0', function() {
        return chai.request(HOST)
        .get(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(testData.users.valid[0].username);
          expect(res.body.payload.email).to.equal(testData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.be.undefined;
          expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(testData.users.valid[0].imageUrl);
          constantUserData.groupIds = res.body.payload.groupIds;
          constantUserData.userId = res.body.payload.userId;
        });
      });

      it('should update user_0', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.valid.oneFieldUsername)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
        });
      });

      it('should get the updated user data of user_0', function() {
        return chai.request(HOST)
        .get(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(testData.users.update.valid.oneFieldUsername.username);
          expect(res.body.payload.email).to.equal(testData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.equal(constantUserData.groupIds);
          expect(res.body.payload.userId).to.equal(constantUserData.userId);
          expect(res.body.payload.role).to.equal('user');
          expect(res.body.payload.imageUrl).to.equal(testData.users.valid[0].imageUrl);
        });
      });
    });

    describe('with error', function() {
      let token;

      before('Clean DB and register User 0', done => {
        databaseHelper.promiseResetDB().then(()=> {
          return registerUser(0);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      it('should fail to update due to missing payload', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update userId', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateUserId)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update groupd ids', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateGroupIds)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update internal id', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateInternalId)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update authType', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateAuthType)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update role', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateRole)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update unknown field', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateUnknownField)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update username with null', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateUsernameNull)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update password with null', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updatePasswordNull)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should fail to update email with null', function() {
        return chai.request(HOST)
        .put(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + token)
        .send(testData.users.update.invalid.updateEmailNull)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });
    });
  });
});
