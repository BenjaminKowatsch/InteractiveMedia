/* jshint expr: true */

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('../data/databaseHelper');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  BASE_GROUP: '/v1/groups/',
  REGISTER_USER: '/v1/users/',
};

const userData = require('../data/user.data');
const groupScenarios = require('./../data/groupScenarios');

// ************* Helper ***********//

var registerUser = index => chai.request(HOST).post(URL.REGISTER_USER).send({
  username: userData.users.valid[index].username,
  email: userData.users.valid[index].email,
  password: userData.users.valid[index].password
});

describe.only('Groups-Controller', () => {
  // this.timeout(5000); // How long to wait for a response (ms)

  before('register User 0 and 1', done => {
    tokens = {};
    databaseHelper.resetDB().then(()=> {
      return registerUser(0);
    }).then(res => {
      tokens[0] = res.body.payload.accessToken;
      return registerUser(1);
    }).then(res => {
      tokens[1] = res.body.payload.accessToken;
      done();
    }).catch((error) => {
      console.log('Register User Error: ' + error);
    });
  });

  it('should create a new group', () => {
    return chai.request(HOST)
      .post(URL.BASE_GROUP)
      .send({'accessToken': tokens[0], 'authType': 0, payload: groupScenarios[0].create})
      .then(function(res) {
        expect(res).to.have.status(201);
        expect(res).to.be.json;
      });
  });

  it.skip('should not create a new group due to wrong token', () => {
    return chai.request(HOST)
      .get(URL.BASE_GROUP)
      .send({'accessToken': 'fooBar', 'authType': 0, payload: groupScenarios[0].create})
      .catch(res => {
        expect(res).to.have.status(402);
        expect(res).to.be.json;
      });
  });

  it.skip('should not create a new group due to missing token', () => {
    return chai.request(HOST)
      .get(URL.BASE_GROUP)
      .send({payload: groupScenarios[0].create})
      .catch(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
      });
  });

  it.skip('should respond with 403 if all groups are accessed as nonAdmin', () => {
    return chai.request(HOST).
      get(URL.BASE + '/').
      send({'accessToken': defaultToken, 'authType': 0}).
      then(function(res) {
        expect(res).to.have.status(403);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.payload).to.equal('Admin access required');
        expect(res.body.success).to.be.false;
      });
  });

  /*
  it('should respond with 200 if post data is correct',
      function() {
        return chai.request(HOST).post(baseUrl + '/group').send({
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
          console.log('group-response: ' + JSON.stringify(res));
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
        });
      });
*/
  it.skip('should deny access to users not in group', () => {
    return chai.request(HOST).
        get(baseUrl + '/group').
        send({'accessToken': alternativeToken, 'authType': 0}).
        then(function(res) {
          expect(res).to.have.status(403);
        });
  });
});
