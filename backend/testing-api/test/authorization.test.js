'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');

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

var registerUser = index => chai.request(HOST).post(URL.BASE_USER).send({
  username: userData.users.valid[index].username,
  email: userData.users.valid[index].email,
  password: userData.users.valid[index].password
});

describe('Authorization', () => {
  describe('No authorization required', () => {
    let userToken;
    let adminToken;
    before('register User 0', done => {
      registerUser(0).then(res => {
        userToken = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    before('add admin', done => {
      databaseHelper.promiseResetDB().then(() => {
        return chai.request(HOST).post(URL.BASE_ADMIN + '/add');
      }).then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
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
    before('add admin', done => {
      databaseHelper.promiseResetDB().then(() => {
        return chai.request(HOST).post(URL.BASE_ADMIN + '/add');
      }).then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
    });
    before('register User 1', done => {
      registerUser(1).then(res => {
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
            expect(res).to.have.status(401);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.be.equal('authentication');
            expect(res.body.payload.message).to.be.equal('no http request header Authorization provided');
          });
    });

    it('should fail with normal user', () => {
      return chai.request(HOST)
          .get(URL.TEST_AUTHORIZATION + '/admin')
          .set('Authorization', '0 ' + userToken)
          .then(res => {
            expect(res).to.have.status(403);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.be.equal('authorization');
            expect(res.body.payload.message).to.be.equal('user is not authorized');
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
