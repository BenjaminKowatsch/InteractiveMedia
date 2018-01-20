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
  BASE_USER: '/v1/users',
  TEST_AUTHENTICATION: '/v1/test/authentication',
  TEST_AUTHORIZATION: '/v1/test/authorization',
  BASE_ADMIN: '/v1/admin'
};

const userData = require('./data/user.data');
const adminData = require('./data/admin.data');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.BASE_USER).send(userData.users.valid[index]);

describe('Authorization', () => {
  describe('No authorization required', () => {
    let userToken;
    let adminToken;

    before('reset db', done => {
      databaseHelper.promiseResetDB().then(() => {done();})
        .catch((err) => {console.error('Error add admin');});
    });

    before('login admin', done => {
      chai.request(HOST).post(URL.BASE_USER + '/login?type=0')
          .send({username: adminData.username, password: adminData.password})
      .then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
    });

    before('register User 0', done => {
      registerUser(0).then(res => {
        userToken = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should be accessible with no authorization', () => {
        return chai.request(HOST)
            .get(URL.TEST_AUTHORIZATION + '/none')
            .then(res => {
              expect(res).to.have.status(200);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.hello).to.be.equal('open world without authorization');
            });
      });
    it('should be accessible for normal user', () => {
        return chai.request(HOST)
            .get(URL.TEST_AUTHORIZATION + '/none')
            .set('Authorization', '0 ' + userToken)
            .then(res => {
              expect(res).to.have.status(200);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.hello).to.be.equal('open world without authorization');
            });
      });
    it('should be accessible for normal admin', () => {
        return chai.request(HOST)
            .get(URL.TEST_AUTHORIZATION + '/none')
            .set('Authorization', '0 ' + adminToken)
            .then(res => {
              expect(res).to.have.status(200);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.hello).to.be.equal('open world without authorization');
            });
      });
  });

  describe('required to be Admin', () => {
    let adminToken;
    let userToken;

    before('reset db', done => {
      databaseHelper.promiseResetDB().then(() => {done();})
        .catch((err) => {console.error('Error add admin');});
    });

    before('login admin', done => {
      chai.request(HOST).post(URL.BASE_USER + '/login?type=0')
          .send({username: adminData.username, password: adminData.password})
      .then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
    });

    before('register User 0', done => {
      registerUser(0).then(res => {
        userToken = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should fail with no authorization header', () => {
      return chai.request(HOST)
          .get(URL.TEST_AUTHORIZATION + '/admin')
          .then(res => {
            expectResponse.toBe401.missingHeaderAuthorization(res);
          });
    });

    it('should fail with normal user', () => {
      return chai.request(HOST)
          .get(URL.TEST_AUTHORIZATION + '/admin')
          .set('Authorization', '0 ' + userToken)
          .then(res => {
            expectResponse.toBe403.unauthorized(res);
          });
    });

    it('should be accessible for admin', () => {
        return chai.request(HOST)
            .get(URL.TEST_AUTHORIZATION + '/admin')
            .set('Authorization', '0 ' + adminToken)
            .then(res => {
              expect(res).to.have.status(200);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.true;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.hello).to.be.equal('authorized world as admin');
            });
      });
  });
});
