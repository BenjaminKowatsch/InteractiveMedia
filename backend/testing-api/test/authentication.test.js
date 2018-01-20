'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');
const expectResponse = require('../util/expectResponse.util');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  REGISTER_USER: '/v1/users',
  TEST_AUTHENTICATION: '/v1/test/authentication'
};

const userData = require('./data/user.data');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.REGISTER_USER).send(userData.users.valid[index]);

describe('Autentication', function() {
  describe('No autentication required', function() {
    it('should require no authentication', function() {
        return chai.request(HOST)
            .get(URL.TEST_AUTHENTICATION + '/none')
            .then(res => {
              expect(res).to.have.status(200);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.hello).to.be.equal('open world');
            });
      });
  });

  describe('autentication required', function() {
    let token;
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

    it('should require authentication', function() {
        return chai.request(HOST)
            .get(URL.TEST_AUTHENTICATION + '/required')
            .set('Authorization', '0 ' + token)
            .then(res => {
              expect(res).to.have.status(200);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.hello).to.be.equal('authenticated world');
            });
      });

    it('should fail to get restricted resource with no authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .then(res => {
            expectResponse.toBe401.missingHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with no token in authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .set('Authorization', '0')
          .then(res => {

          });
    });

    it('should fail to get restricted resource with no authType in authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .set('Authorization', token)
          .then(res => {
            expectResponse.toBe401.invalidFormatHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with too many arguments in authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .set('Authorization', '0 ' +  token + ' XXX')
          .then(res => {
            expectResponse.toBe401.invalidFormatHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with non-integer authType in authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .set('Authorization', 'X ' + token)
          .then(res => {
            expect(res).to.have.status(401);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.be.equal('authentication');
            expect(res.body.payload.message).to.be.
              equal('invalid authType provided in http request header Authorization');
          });
    });

    it('should fail to get restricted resource with invalid authType in authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .set('Authorization', '99 ' + token)
          .then(res => {
            expect(res).to.have.status(401);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.be.equal('authentication');
            expect(res.body.payload.message).to.be.
              equal('invalid authType provided in http request header Authorization');
          });
    });

    it('should fail to get restricted resource with invalid auth token in authorization header', function() {
      return chai.request(HOST)
          .get(URL.TEST_AUTHENTICATION + '/required')
          .set('Authorization', '0 ' + 'XXX')
          .then(res => {
            expectResponse.toBe401.invalidAuthToken(res);
          });
    });

    it('should fail to get restricted resource with valid auth token and wrong authType in authorization header',
      function() {
      return chai.request(HOST)
      .get(URL.TEST_AUTHENTICATION + '/required')
      .set('Authorization', '1 ' + token)
      .then(res => {
        expectResponse.toBe401.invalidAuthToken(res);
      });
    }).timeout(10000);

  });
});
