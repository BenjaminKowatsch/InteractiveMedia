/* jshint expr: true */

const chai = require('chai');
const expect = require('chai').expect;

chai.use(require('chai-http'));

const host = 'http://backend:8081';
const baseUrl = '/v1/groups';
const loginUrl = '/v1/users/';

const testData = require('../data/users');

describe('Get groups', function() {
  this.timeout(5000); // How long to wait for a response (ms)
  var defaultToken;
  var alternativeToken;
  before(function(done) {
    // first register a new default user
    chai.request(host).post(loginUrl).send({
      username: testData.users.valid[2].username,
      password: testData.users.valid[2].password,
    }).then(function(res) {
      defaultToken = res.body.payload.accessToken;
    })
    // then request a valid access token from facebook
        .catch((error) => {
          console.log('Facbook Login Error: ' + error);
        });
    chai.request(host).post(loginUrl).send({
      username: testData.users.valid[3].username,
      password: testData.users.valid[3].password,
    }).then(function(res) {
      alternativeToken = res.body.payload.accessToken;
      done();
    })
    // then request a valid access token from facebook
        .catch((error) => {
          console.log('Facbook Login Error: ' + error);
          done();
        });
  });

  it('should respond with 403 if all groups are accessed as nonAdmin',
      function() {
        return chai.request(host).
            get(baseUrl + '/').
            send({'accessToken': defaultToken, 'authType': 0}).
            then(function(res) {
              expect(res).to.have.status(403);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.payload).to.equal('Admin access required');
              expect(res.body.success).to.be.false;
            });
      });

  it('should respond with 200 if post data is correct',
      function() {
        return chai.request(host).post(baseUrl + '/group').send({
          'accessToken': defaultToken,
          'authType': 0,
          'payload': {
            'objectId': null,
            'createdAt': null,
            'name': 'Group 1',
            'imageUrl': 'http://blabla.de/bla.png',
            'users': [],
            'transactions': [],
          },
        }).then(function(res) {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
        });
      });

  it('should deny access to users not in group'),
      function() {
        return chai.request(host).
            get(baseUrl + '/group').
            send({'accessToken': alternativeToken, 'authType': 0}).
            then(function(res) {
              expect(res).to.have.status(403);
            });
      };
});
