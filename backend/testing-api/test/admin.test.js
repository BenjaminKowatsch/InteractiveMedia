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
  BASE_ADMIN: '/v1/admin',
  BASE_GROUP: '/v1/groups'
};

const userData = require('./data/user.data');
const adminData = require('./data/admin.data');
const groupScenarios = require('./data/groupScenarios');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.BASE_USER).send({
  username: userData.users.valid[index].username,
  email: userData.users.valid[index].email,
  password: userData.users.valid[index].password
});

describe('Admin', () => {
  describe('Login', () => {
    before('reset db', done => {
      databaseHelper.promiseResetDB().then(() => {done();})
      .catch((err) => {console.error('Error add admin');});
    });

    it('should login as admin', () => {
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

    it('should get user data of admin', () => {
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

  describe('Groups', () => {
    describe('get all groups', () => {
      let adminToken;
      let tokens = {};
      let groupIds = {};
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

      before('register users, create groups', done => {
        registerUser(0).then(res => {
          tokens[0] = res.body.payload.accessToken;
          return registerUser(1);
        }).then(res => {
          tokens[1] = res.body.payload.accessToken;
          return registerUser(2);
        }).then(res => {
          tokens[2] = res.body.payload.accessToken;
          return chai.request(HOST)
            .post(URL.BASE_GROUP  + '/')
            .set('Authorization', '0 ' + tokens[0])
            .send(groupScenarios[1].createGroup0);
        }).then(res => {
          groupIds[0] = res.body.payload.groupId;
          return chai.request(HOST)
            .post(URL.BASE_GROUP  + '/')
            .set('Authorization', '0 ' + tokens[0])
            .send(groupScenarios[1].createGroup1);
        }).then(res => {
          groupIds[1] = res.body.payload.groupId;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      it('should get all groups', () => {
        return chai.request(HOST)
        .get(URL.BASE_ADMIN + '/groups')
        .set('Authorization', '0 ' + adminToken)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array');
          expect(res.body.payload).to.have.lengthOf(2);
          expect(res.body.payload[0].name).to.equal(groupScenarios[1].createGroup0.name);
          expect(res.body.payload[0].imageUrl).to.equal(groupScenarios[1].createGroup0.imageUrl);
          expect(res.body.payload[0].groupId).to.equal(groupIds[0]);
          expect(res.body.payload[0].createdAt).to.be.an('string').and.not.to.be.empty;
          expect(res.body.payload[0].countUsers).to.equal(groupScenarios[1].createGroup0.users.length);
          expect(res.body.payload[0].countTransactions).to.equal(0);
          expect(res.body.payload[1].name).to.equal(groupScenarios[1].createGroup1.name);
          expect(res.body.payload[1].imageUrl).to.equal(groupScenarios[1].createGroup1.imageUrl);
          expect(res.body.payload[1].groupId).to.equal(groupIds[1]);
          expect(res.body.payload[1].createdAt).to.be.an('string').and.not.to.be.empty;
          expect(res.body.payload[1].countUsers).to.equal(groupScenarios[1].createGroup1.users.length);
          expect(res.body.payload[1].countTransactions).to.equal(0);
        });
      });

      it('should fail to get all groups with normal user', () => {
        return chai.request(HOST)
            .get(URL.BASE_ADMIN + '/groups')
            .set('Authorization', '0 ' + tokens[0])
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
    });

    describe('get group', () => {
      let adminToken;
      let tokens = {};
      let groupId;
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

      before('register users, create group', done => {
        registerUser(0).then(res => {
          tokens[0] = res.body.payload.accessToken;
          return registerUser(1);
        }).then(res => {
          tokens[1] = res.body.payload.accessToken;
          return registerUser(2);
        }).then(res => {
          tokens[2] = res.body.payload.accessToken;
          return chai.request(HOST)
            .post(URL.BASE_GROUP  + '/')
            .set('Authorization', '0 ' + tokens[0])
            .send(groupScenarios[1].createGroup0);
        }).then(res => {
          groupId = res.body.payload.groupId;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      it('should get group by id', () => {
        return chai.request(HOST)
        .get(URL.BASE_ADMIN + '/groups/' + groupId)
        .set('Authorization', '0 ' + adminToken)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.name).to.equal(groupScenarios[1].createGroup0.name);
          expect(res.body.payload.imageUrl).to.equal(groupScenarios[1].createGroup0.imageUrl);
          expect(res.body.payload.users).to.have.lengthOf(groupScenarios[1].createGroup0.users.length);
          expect(res.body.payload.users.map(val => val.username))
          .to.have.members([userData.users.valid[0].username, userData.users.valid[1].username]);
          expect(res.body.payload.transactions).to.be.empty;
          expect(res.body.payload.groupId).to.equal(groupId);
          expect(res.body.payload.createdAt).to.be.an('string').and.not.to.be.empty;
        });
      });

      it('should fail to get group by id with normal user', () => {
        return chai.request(HOST)
            .get(URL.BASE_ADMIN + '/groups/' + groupId)
            .set('Authorization', '0 ' + tokens[0])
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

      it('should fail to get group by id with unknown groupId', () => {
        return chai.request(HOST)
            .get(URL.BASE_ADMIN + '/groups/' + 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX')
            .set('Authorization', '0 ' + adminToken)
            .then(res => {
              expect(res).to.have.status(404);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.false;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.dataPath).to.be.equal('group');
              expect(res.body.payload.message).to.be.equal('group not found');
            });
      });
    });
  });

  describe('Users', () => {
    describe('get all users', () => {
      let adminToken;
      let tokens = {};
      let groupId;
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

      before('register users, create group', done => {
        registerUser(0).then(res => {
          tokens[0] = res.body.payload.accessToken;
          return registerUser(1);
        }).then(res => {
          tokens[1] = res.body.payload.accessToken;
          return chai.request(HOST)
            .post(URL.BASE_GROUP  + '/')
            .set('Authorization', '0 ' + tokens[0])
            .send(groupScenarios[1].createGroup0);
        }).then(res => {
          groupId = res.body.payload.groupId;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      it('should get all users', () => {
        return chai.request(HOST)
        .get(URL.BASE_ADMIN + '/users')
        .set('Authorization', '0 ' + adminToken)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array');
          expect(res.body.payload).to.have.lengthOf(3);
          expect(res.body.payload[0].username).to.equal(adminData.username);
          expect(res.body.payload[0].email).to.equal(adminData.email);
          expect(res.body.payload[0].userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload[0].role).to.equal('admin');
          expect(res.body.payload[0].countGroupIds).to.equal(0);
          expect(res.body.payload[1].username).to.equal(userData.users.valid[0].username);
          expect(res.body.payload[1].email).to.equal(userData.users.valid[0].email);
          expect(res.body.payload[1].userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload[1].role).to.equal('user');
          expect(res.body.payload[1].countGroupIds).to.equal(1);
          expect(res.body.payload[2].username).to.equal(userData.users.valid[1].username);
          expect(res.body.payload[2].email).to.equal(userData.users.valid[1].email);
          expect(res.body.payload[2].userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload[2].role).to.equal('user');
          expect(res.body.payload[2].countGroupIds).to.equal(1);
        });
      });

      it('should fail to get all users with normal user', () => {
        return chai.request(HOST)
            .get(URL.BASE_ADMIN + '/users')
            .set('Authorization', '0 ' + tokens[0])
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
    });

    describe('get user', () => {
      let adminToken;
      let tokens = {};
      let userIds = {};
      let groupId;
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

      before('register users, create group', done => {
        registerUser(0).then(res => {
          tokens[0] = res.body.payload.accessToken;
          return registerUser(1);
        }).then(res => {
          tokens[1] = res.body.payload.accessToken;
          return registerUser(2);
        }).then(res => {
          tokens[2] = res.body.payload.accessToken;
          return chai.request(HOST)
            .post(URL.BASE_GROUP  + '/')
            .set('Authorization', '0 ' + tokens[0])
            .send(groupScenarios[1].createGroup0);
        }).then(res => {
          groupId = res.body.payload.groupId;
          done();
        }).catch((error) => {console.log('Register User Error: ' + error);});
      });

      before('get userId of user_0', done => {
        chai.request(HOST)
        .get(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + tokens[0])
        .then(res => {
          userIds[0] = res.body.payload.userId;
          done();
        }).catch(error => {console.error('Unable to get userId of user_0');});
      });

      it('should get user by id', () => {
        return chai.request(HOST)
        .get(URL.BASE_ADMIN + '/users/' + userIds[0])
        .set('Authorization', '0 ' + adminToken)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(userData.users.valid[0].username);
          expect(res.body.payload.email).to.equal(userData.users.valid[0].email);
          expect(res.body.payload._id).to.be.undefined;
          expect(res.body.payload.groupIds).to.be.an('array');
          expect(res.body.payload.groupIds[0]).to.equal(groupId);
          expect(res.body.payload.userId).to.equal(userIds[0]);
          expect(res.body.payload.role).to.equal('user');
        });
      });

      it('should fail to get user by id with normal user', () => {
        return chai.request(HOST)
            .get(URL.BASE_ADMIN + '/users/' + userIds[0])
            .set('Authorization', '0 ' + tokens[0])
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

      it('should fail to get user by id with unknown userId', () => {
        return chai.request(HOST)
            .get(URL.BASE_ADMIN + '/users/' + 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX')
            .set('Authorization', '0 ' + adminToken)
            .then(res => {
              expect(res).to.have.status(404);
              expect(res).to.be.json;
              expect(res.body).to.be.an('object');
              expect(res.body.success).to.be.false;
              expect(res.body.payload).to.be.an('object');
              expect(res.body.payload.dataPath).to.be.equal('user');
              expect(res.body.payload.message).to.be.equal('user not found');
            });
      });
    });
  });
});
