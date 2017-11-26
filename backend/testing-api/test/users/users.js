/* jshint expr: true */

var chai = require('chai');
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = 'http://backend:8081';
var baseUrl = '/v1/users';

var https = require('https');
var config = {
  'facebookUrlAppToken': process.env.FACEBOOK_URL_APP_TOKEN,
  'facebookAppId': process.env.FACEBOOK_APP_ID
};

var testData = {
  'users': {
    'valid': [
      {
        'username': 'alex1',
        'password': 'alexpassword'
      }
    ],
    'invalid': {
      'notExistingUser': {
        'username': 'alexinvalid',
        'password': 'pwdpwd'
      },
      'invalidUsername': {
        'username': 's',
        'password': 'pwdpwd'
      },
      'invalidPassword': {
        'username': 'alex12',
        'password': 'p'
      }
    }
  }
};

function log(message) {
  console.log(message);
}

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

describe('Login', function() {
  var accessToken;
  before(function(done) {
    getFacebookTestAccessToken()
    .then((token) => {
      log('Facbook Login got access token: ' + token);
      accessToken = token;
      done();
    }).catch((error) => {
      log('Facbook Login Error: ' + error);
      done();
    });
  });

  // POST - Register new user
  it('Login facebook user', function() {
    return chai.request(host)
          .post(baseUrl + '/login')
          .send({'accessToken': accessToken, 'authType': 2, 'payload': {}})
          .then(res => {
            expect(res).to.have.status(201);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });
});

describe('Register with username + password', function() {
  this.timeout(5000); // How long to wait for a response (ms)

  before(function() {});
  after(function() {});

  // POST - Register new user
  it('should register new user', function() {
    return chai.request(host)
       .post(baseUrl + '/')
       .send({username: testData.users.valid[0].username, password: testData.users.valid[0].password})
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

  // POST - Register existing user
  it('should fail to register existing user', function() {
    return chai.request(host)
        .post(baseUrl + '/')
        .send({username: testData.users.valid[0].username, password: testData.users.valid[0].password})
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

  // POST - Register invalid user
  it('should fail to register user with invalid username', function() {
    return chai.request(host)
        .post(baseUrl + '/')
        .send({username: testData.users.invalid.invalidUsername.username,
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

  // POST - Register invalid user
  it('should fail to register user with invalid password', function() {
    return chai.request(host)
        .post(baseUrl + '/')
        .send({username: testData.users.invalid.invalidPassword.username,
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
