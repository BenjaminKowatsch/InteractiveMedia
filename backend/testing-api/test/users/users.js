/* jshint expr: true */

var chai = require('chai');
var fs = require('fs');
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = 'http://backend:8081';
var baseUrl = '/v1/users';

var https = require('https');
var config = {
  'facebookUrlAppToken': process.env.FACEBOOK_URL_APP_TOKEN,
  'facebookAppId': process.env.FACEBOOK_APP_ID
};

const testData = require('../data/users');

function getFacebookTestAccessToken() {
  return new Promise((resolve, reject) => {
    https.get('https://graph.facebook.com/v2.11/' + config.facebookAppId + '/' +
    'accounts/test-users?access_token=' +
    config.facebookUrlAppToken, function(response) {
      var responseMessage = '';

      response.on('data', function(chunk) {
        responseMessage += chunk;
      });

      response.on('end', function() {
        var data = JSON.parse(responseMessage);
        if (data.length <= 0) {
          reject(data);
        } else {
          resolve(data.data[0].access_token);
        }
      });
    });
  });
}

describe('Auth-Type: Facebook', function() {
  var facebookToken;
  before(function(done) {
    getFacebookTestAccessToken()
      .then((token) => {
        console.log('Facbook Login got access token: ' + token);
        facebookToken = token;
        done();
      }).catch((error) => {
        console.log('Facbook Login Error: ' + error);
        done();
      });
  });

  // POST - Login/Register new facebook user
  it('Login/Register as facebook user', function() {
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
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
    return chai.request(host)
          .post(baseUrl + '/sendData')
          .send({'accessToken': facebookToken, 'authType': 2, 'payload': {}})
          .then(res => {
            expect(res).to.have.status(201);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });

  // POST - Logout as default user
  it('Logout as facebook user', function() {
    return chai.request(host)
            .post(baseUrl + '/logout')
            .send({'accessToken': facebookToken, 'authType': 2})
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
    return chai.request(host)
          .post(baseUrl + '/sendData')
          .send({'accessToken': facebookToken, 'authType': 2, 'payload': {}})
          .then(res => {
            expect(res).to.have.status(401);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
          });
  });

  // POST - Relogin facebook user
  it('Relogin as facebook user', function() {
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
          .send({'accessToken': facebookToken})
          .then(res => {
            expect(res).to.have.status(200);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });

  it.skip('should fail to login with invalid token', function() {
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
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
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
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
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
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
    it('should register new user', function() {
      return chai.request(host)
        .post(baseUrl + '/')
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
      return chai.request(host)
          .post(baseUrl + '/')
          .send({username: testData.users.valid[0].username,
            email: testData.users.valid[0].email,
            password: testData.users.valid[0].password})
          .then(function(res) {
            expect(res).to.have.status(400);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.equal('username');
            expect(res.body.payload.message).to.equal('Username already exists');
          });
    });

    it('should fail to register user with invalid username', function() {
      return chai.request(host)
          .post(baseUrl + '/')
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
      return chai.request(host)
          .post(baseUrl + '/')
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
    // TODO: drop and recreate database
    let defaultToken;

    before(function(done) {
      chai.request(host)
        .post(baseUrl + '/')
        .send({username: testData.users.valid[1].username,
          email: testData.users.valid[1].email,
          password: testData.users.valid[1].password})
        .then((res) => {
          defaultToken = res.body.payload.accessToken;
          done();
        });
    });

    it('should login as registered user', function() {
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
                expect(res.body.payload.accessToken).to.have.lengthOf(84);
              });
    });

    it('should fail to login with invalid password', function() {
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
    // TODO: drop and recreate database
    let defaultToken;

    before(function(done) {
      chai.request(host)
        .post(baseUrl + '/')
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
      console.log('defaultToken', defaultToken);
      return chai.request(host)
        .post(baseUrl + '/logout')
        .send({'accessToken': defaultToken, 'authType': 0})
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
      return chai.request(host)
            .post(baseUrl + '/sendData')
            .send({'accessToken': defaultToken, 'authType': 0, 'payload': {}})
            .then(res => {
              expect(res).to.have.status(401);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.false;
            });
    });

    // POST - Relogin as default user
    it('should re-login', function() {
      return chai.request(host)
              .post(baseUrl + '/login?type=0')
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
  it('should fail with an invalid auth type', function() {
    return chai.request(host)
            .post(baseUrl + '/login?type=99')
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
    return chai.request(host)
            .post(baseUrl + '/login')
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
