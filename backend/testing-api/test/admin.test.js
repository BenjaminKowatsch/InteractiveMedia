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

describe('Admin', () => {
  describe('Login', () => {
    before('add admin', done => {
      databaseHelper.promiseResetDB().then(() => {
        return chai.request(HOST).post(URL.BASE_ADMIN + '/add');
      }).then(res => {done();})
      .catch((err) => {console.error('Error add admin');});
    });

    it('should login as admin', function() {
      return chai.request(HOST)
      .post(URL.BASE_USER + '/login?type=0')
      .send({username: adminData.username, password: adminData.password})
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
  });

  describe('User data', () => {
    let adminToken;
    before('add admin and login', done => {
      databaseHelper.promiseResetDB().then(() => {
        return chai.request(HOST).post(URL.BASE_ADMIN + '/add');
      }).then(res => {
        return chai.request(HOST).post(URL.BASE_USER + '/login?type=0')
          .send({username: adminData.username, password: adminData.password});
      }).then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
    });

    it('should get user data of admin', function() {
      return chai.request(HOST)
      .get(URL.BASE_USER + '/user')
      .set('Authorization', '0 ' + adminToken)
      .then(res => {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.username).to.equal(adminData.username);
        expect(res.body.payload.email).to.equal(adminData.email);
        expect(res.body.payload._id).to.be.undefined;
        expect(res.body.payload.groupIds).to.be.undefined;
        expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
        expect(res.body.payload.role).to.equal('admin');
      });
    });
  });
});
