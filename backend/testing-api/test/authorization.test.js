'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');
const expectResponse = require('../util/expectResponse.util');
const settings = require('../config/settings.config');
const userService = require('../util/userService.util');

chai.use(require('chai-http'));

const userData = require('./data/user.data');
const adminData = require('./data/admin.data');

describe('Authorization', () => {
  describe('No authorization required', () => {
    let userToken;
    let adminToken;

    before('reset db', done => {
      databaseHelper.promiseResetDB().then(() => {done();})
        .catch((err) => {console.error('Error add admin');});
    });

    before('login admin', done => {
      chai.request(settings.host).post(settings.url.users.base + '/login?type=0')
          .send({username: adminData.username, password: adminData.password})
      .then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
    });

    before('register User 0', done => {
      userService.register(userData.users.valid[0]).then(res => {
        userToken = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should be accessible with no authorization', () => {
        return chai.request(settings.host)
            .get(settings.url.test.authorization + '/none')
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
        return chai.request(settings.host)
            .get(settings.url.test.authorization + '/none')
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
        return chai.request(settings.host)
            .get(settings.url.test.authorization + '/none')
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
      chai.request(settings.host).post(settings.url.users.base + '/login?type=0')
          .send({username: adminData.username, password: adminData.password})
      .then(res => {
          adminToken = res.body.payload.accessToken;
          done();
        }).catch((err) => {console.error('Error add admin');});
    });

    before('register User 0', done => {
      userService.register(userData.users.valid[0]).then(res => {
        userToken = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should fail with no authorization header', () => {
      return chai.request(settings.host)
          .get(settings.url.test.authorization + '/admin')
          .then(res => {
            expectResponse.toBe401.missingHeaderAuthorization(res);
          });
    });

    it('should fail with normal user', () => {
      return chai.request(settings.host)
          .get(settings.url.test.authorization + '/admin')
          .set('Authorization', '0 ' + userToken)
          .then(res => {
            expectResponse.toBe403.unauthorized(res);
          });
    });

    it('should be accessible for admin', () => {
        return chai.request(settings.host)
            .get(settings.url.test.authorization + '/admin')
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
