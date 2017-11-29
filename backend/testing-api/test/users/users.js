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

var testData = {
  'users': {
    'valid': [
      {
        'username': 'alex1',
        'password': 'alexpassword'
      },
      {
        'username': 'benny1',
        'password': 'benny1password'
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
/*
describe('Test object storage api', function() {
  // POST - Send facebook access token
  it('Upload image', function() {
    return chai.request(host)
            .post('/v1/object-store' + '/upload?filename="image.png"')
            .attach('uploadField', fs.readFileSync('image.png'), 'image.png')
            .then(res => {
              console.log(JSON.stringify(res));
            });
  });

    it('Download image', function() {
      return chai.request(host)
              .get('/v1/object-store' + '/download')
              .then(res => {
                console.log(JSON.stringify(res));
              });
    });
});
*/
describe('Login and send user data', function() {
  var facebookToken;
  var defaultToken;
  before(function(done) {
    // first register a new default user
    chai.request(host)
    .post(baseUrl + '/')
    .send({username: testData.users.valid[1].username, password: testData.users.valid[1].password})
    .then(function(res) {
      defaultToken = res.body.payload.accessToken;
      log('Default token: ' + defaultToken);
      return getFacebookTestAccessToken();
    })
    // then request a valid access token from facebook
    .then((token) => {
      log('Facbook Login got access token: ' + token);
      facebookToken = token;
      done();
    }).catch((error) => {
      log('Facbook Login Error: ' + error);
      done();
    });
  });

  // POST - Send facebook access token
  it('Login as default user', function() {
    return chai.request(host)
            .post(baseUrl + '/login?type=0')
            .send({username: testData.users.valid[1].username, password: testData.users.valid[1].password})
            .then(res => {
              expect(res).to.have.status(201);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
            });
  });

  // POST - Login/Register new facebook user
  it('Login/Register as facebook user', function() {
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
          .send({'accessToken': facebookToken})
          .then(res => {
            expect(res).to.have.status(201);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });

  /*
  var idToken = 'eyJhbGciOiJSUzI1NiIsImtpZCI6IjdmMTIxZDQ1MTlmNjY3ZmM5Zjc0ZDhmMmIyZTUwODI5YTU1' +
  'MTk3YjcifQ.eyJhenAiOiI5MDgxNzA0NzA4NzMtODE1MXM3Y3ZsZHJvZWJkbDdtY3' +
  'BnMmhvdWRnbGpnZmcuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI5' +
  'MDgxNzA0NzA4NzMtODE1MXM3Y3ZsZHJvZWJkbDdtY3BnMmhvdWRnbGpnZmcuYXBwcy5' +
  'nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTU5MTM2MDQ4MjUxNTM3NzU1NDE' +
  'iLCJhdF9oYXNoIjoiSmdETnh0RnZaSkVuSGVDS0VDZTBzdyIsImlzcyI6Imh0dHBzOi8v' +
  'YWNjb3VudHMuZ29vZ2xlLmNvbSIsImlhdCI6MTUxMTc4NTMxNCwiZXhwIjoxNTExNzg4OTE' +
  '0LCJuYW1lIjoiQmVubnkgSy4iLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDUuZ29vZ2xldXNlcmN' +
  'vbnRlbnQuY29tLy1DRzJ4ZktpZVhtOC9BQUFBQUFBQUFBSS9BQUFBQUFBQUFBQS9BRmlZb2Ywd' +
  'kF2dDh4QS1GTkN6N01CWDFWTk5DdjdaMU9RL3M5Ni1jL3Bob3RvLmpwZyIsImdpdmVuX25hbW' +
  'UiOiJCZW5ueSIsImZhbWlseV9uYW1lIjoiSy4iLCJsb2NhbGUiOiJkZSJ9.MD7ylstr0lzc-B' +
  'fvyF8q2BJm5r-c8gN5gbpD0xPbZfwr-DuXbBHXOYH6iIzkg1qF0EzLm4zDvzGiy3SD_QJviRj' +
  'QsAflEJHM8CVbkM5zU-nSZNLHSTApqGu1AHXOjlqkyCuX3eaf3p61twG8ApE-BWLL2Inrh8wo' +
  'TD0qPeDMb-2ZAo0bS8_qN1X5URfuaqSV94qH-9yFYUdKUP0wGaZzo-6oFEz3ArqdLI883KhOs' +
  'B0Wvl41i9MHBfGpnEw8BtJF46Hd9kW9CDdo_u_ScjqGGbOD8BLiJ5FbS8gkBklC3vLa54I3dj' +
  'PvYqfYOKQXsr9TrejSse6uI-mFiYZa-wJ3Hg';

  // POST - Login/Register new facebook user
  it('Login/Register as google user', function() {
    return chai.request(host)
          .post(baseUrl + '/login?type=1')
          .send({'accessToken': idToken})
          .then(res => {
            expect(res).to.have.status(201);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });

  // POST - Logout as google user
  it('Logout as google user', function() {
    return chai.request(host)
          .post(baseUrl + '/logout')
          .send({'accessToken': idToken, 'authType': 1})
          .then(res => {
            expect(res).to.have.status(201);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });
  */
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

  // POST - Send user data as facebook user
  it('Send user data as default user', function() {
    return chai.request(host)
          .post(baseUrl + '/sendData')
          .send({'accessToken': defaultToken, 'authType': 0, 'payload': {}})
          .then(res => {
            expect(res).to.have.status(201);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.true;
            expect(res.body.payload).to.be.an('object');
          });
  });

  // POST - Logout as default user
  it('Logout as default user', function() {
    return chai.request(host)
          .post(baseUrl + '/logout')
          .send({'accessToken': defaultToken, 'authType': 0})
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
              expect(res).to.have.status(201);
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
  it('Relogin as default user', function() {
    return chai.request(host)
            .post(baseUrl + '/login?type=0')
            .send({username: testData.users.valid[1].username, password: testData.users.valid[1].password})
            .then(res => {
              expect(res).to.have.status(201);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
            });
  });

  // POST - Relogin facebook user
  it('Relogin as facebook user', function() {
    return chai.request(host)
          .post(baseUrl + '/login?type=2')
          .send({'accessToken': facebookToken})
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
