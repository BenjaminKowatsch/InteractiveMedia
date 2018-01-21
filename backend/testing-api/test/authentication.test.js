'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseService = require('../util/databaseService');
const expectResponse = require('../util/expectResponse.util');
const settings = require('../config/settings.config');
const userService = require('../util/userService.util');

chai.use(require('chai-http'));

const userData = require('../data/user.data');

describe('Autentication', function() {
  describe('No autentication required', function() {
    it('should require no authentication', function() {
        return chai.request(settings.host)
            .get(settings.url.test.authentication + '/none')
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
      databaseService.promiseResetDB().then(()=> {
        return userService.register(userData.users.valid[0]);
      }).then(res => {
        token = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should require authentication', function() {
        return chai.request(settings.host)
            .get(settings.url.test.authentication + '/required')
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
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .then(res => {
            expectResponse.toBe401.missingHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with no token in authorization header', function() {
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .set('Authorization', '0')
          .then(res => {
            expectResponse.toBe401.invalidFormatHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with no authType in authorization header', function() {
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .set('Authorization', token)
          .then(res => {
            expectResponse.toBe401.invalidFormatHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with too many arguments in authorization header', function() {
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .set('Authorization', '0 ' +  token + ' XXX')
          .then(res => {
            expectResponse.toBe401.invalidFormatHeaderAuthorization(res);
          });
    });

    it('should fail to get restricted resource with non-integer authType in authorization header', function() {
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .set('Authorization', 'X ' + token)
          .then(res => {
            expectResponse.toBe401.invalidAuthType(res);
          });
    });

    it('should fail to get restricted resource with invalid authType in authorization header', function() {
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .set('Authorization', '99 ' + token)
          .then(res => {
            expectResponse.toBe401.invalidAuthType(res);
          });
    });

    it('should fail to get restricted resource with invalid auth token in authorization header', function() {
      return chai.request(settings.host)
          .get(settings.url.test.authentication + '/required')
          .set('Authorization', '0 ' + 'XXX')
          .then(res => {
            expectResponse.toBe401.invalidAuthToken(res);
          });
    });

    it('should fail to get restricted resource with valid auth token and wrong authType in authorization header',
      function() {
      return chai.request(settings.host)
      .get(settings.url.test.authentication + '/required')
      .set('Authorization', '1 ' + token)
      .then(res => {
        expectResponse.toBe401.invalidAuthToken(res);
      });
    }).timeout(10000);

  });
});
