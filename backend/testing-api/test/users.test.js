'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const fs = require('fs');
const expect = require('chai').expect;
const databaseHelper = require('./data/databaseHelper');

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

const registerUser = index => chai.request(HOST).post(URL.BASE_USER).send({
  username: testData.users.valid[index].username,
  email: testData.users.valid[index].email,
  password: testData.users.valid[index].password
});

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
        expect(res.body.payload.dataPath).to.equal('authentication');
        expect(res.body.payload.message).to.equal('invalid auth token');
      });
    });

    it('should fail to login with empty token', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
      .send({'accessToken': ''})
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('accessToken');
      });
    });

    it('should fail to login with empty body', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=2')
      .send({})
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.message).to.be.equal('empty input');
      });
    });
  });

  describe('Auth-Type: Password', function() {
    describe('Register', function() {
      before('Clean DB', databaseHelper.cbResetDB);
      it('should register new user', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send({username: testData.users.valid[0].username,
          email: testData.users.valid[0].email,
          password: testData.users.valid[0].password})
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
        .send({username: testData.users.valid[0].username,
          email: testData.users.valid[0].email,
          password: testData.users.valid[0].password})
        .then(function(res) {
          expect(res).to.have.status(400);
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
        .send({username: testData.users.invalid.invalidUsername.username,
          email: testData.users.invalid.invalidUsername.email,
          password: testData.users.invalid.invalidUsername.password})
        .then(function(res) {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('username');
        });
      });

      it('should fail to register user with invalid password', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send({username: testData.users.invalid.invalidPassword.username,
          email: testData.users.invalid.invalidPassword.email,
          password: testData.users.invalid.invalidPassword.password})
        .then(function(res) {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('password');
        });
      });
    });

    describe('Login', function() {
      before('Clean DB', databaseHelper.cbResetDB);
      let defaultToken;

      before(function(done) {
        chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send({username: testData.users.valid[1].username,
          email: testData.users.valid[1].email,
          password: testData.users.valid[1].password})
        .then((res) => {
          defaultToken = res.body.payload.accessToken;
          done();
        });
      });

      it('should login as registered user', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[1].username,
          email: testData.users.valid[1].email,
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
          email: testData.users.valid[1].email,
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
          email: testData.users.valid[1].email,
          password: ''})
        .then(res => {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('password');
        });
      });

      it('should fail to login with no password', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: testData.users.valid[1].username,
          email: testData.users.valid[1].email})
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

      it('should fail to login with no username', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({email: testData.users.valid[1].email,
          password: testData.users.valid[2].password})
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

      it('should fail to login with unknown username', function() {
        return chai.request(HOST)
        .post(URL.BASE_USER + '/login?type=0')
        .send({username: 'unknownUsername',
          email: 'unknownEmail@example.de',
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
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.message).to.be.equal('empty input');
        });
      });

    });

    describe('Logout', function() {
      before('Clean DB', databaseHelper.cbResetDB);
      let defaultToken;

      before(function(done) {
        chai.request(HOST)
        .post(URL.BASE_USER + '/')
        .send({username: testData.users.valid[4].username,
          email: testData.users.valid[4].email,
          password: testData.users.valid[4].password})
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
          email: testData.users.valid[4].email,
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
        email: testData.users.valid[1].email,
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
        email: testData.users.valid[1].email,
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
      });
    });

    it('should not get the user-data of user_0 due to wrong token', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER  + '/user')
      .set('Authorization', '0 this_is_a_wrong_token')
      .then(res => {
        expect(res).to.have.status(401);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('authentication');
        expect(res.body.payload.message).to.equal('invalid authToken');
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

  describe('Update fcm token', () => {
    let token;
    const fcmToken = 'cUf35139J8U:APA91bH6pkjWHRAUAW52QGQV6tR8SQdbpJK20QitJrAyWfX22VP4G0OUL-' +
    'cwnXQob507qnBILDkZaoY0IW3eAvAevjM5dgCTbL297n1pbXoEHLzNDKV-86xJkle0TR6RBi8fA3BzEEOr';
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

    it('should update fcm token of user_0', function() {
      return chai.request(HOST)
      .put(URL.BASE_USER  + '/user/fcmtoken')
      .set('Authorization', '0 ' + token)
      .send({fcmToken: fcmToken})
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
      });
    });

    it('should fail to update due to missing payload', function() {
      return chai.request(HOST)
      .put(URL.BASE_USER  + '/user/fcmtoken')
      .set('Authorization', '0 ' + token)
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('validation');
        expect(res.body.payload.message).to.equal('Invalid body');
      });
    });
  });
});