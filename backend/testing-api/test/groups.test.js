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
  BASE_GROUP: '/v1/groups',
  BASE_USER: '/v1/users',
};

const userData = require('./data/user.data');
const groupScenarios = require('./data/groupScenarios');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.BASE_USER  + '/').send(userData.users.valid[index]);
const getUserData = token => chai.request(HOST).get(URL.BASE_USER  + '/user').set('Authorization', '0 ' + token);
const deepCopy = data => JSON.parse(JSON.stringify(data));

describe('Groups-Controller: Groups:', () => {
  describe('Create new Group', () => {
    let tokens = {};
    let groupId = {};
    before('register User 0 and 1', done => {
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
        tokens[0] = res.body.payload.accessToken;
        return registerUser(1);
      }).then(res => {
        tokens[1] = res.body.payload.accessToken;
        return registerUser(2);
      }).then(res => {
        tokens[2] = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    describe('with success', () => {
      it('should create a new group', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].create)
        .then(function(res) {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.name).to.equal(groupScenarios[0].create.name);
          expect(res.body.payload.imageUrl).to.equal(groupScenarios[0].create.imageUrl);
          expect(res.body.payload.users).to.have.lengthOf(groupScenarios[0].create.users.length);
          expect(res.body.payload.users.map(val => val.username))
          .to.have.members([userData.users.valid[0].username, userData.users.valid[1].username]);
          expect(res.body.payload.users.map(val => val.email))
          .to.have.members([userData.users.valid[0].email, userData.users.valid[1].email]);
          expect(res.body.payload.transactions).to.be.empty;
          expect(res.body.payload.groupId).to.be.an('string').and.not.to.be.empty;
          expect(res.body.payload.createdAt).to.be.an('string').and.not.to.be.empty;
          groupId = res.body.payload.groupId;
        });
      });

      it('should get the groupId with userDate request of user_0', function() {
        return chai.request(HOST)
        .get(URL.BASE_USER  + '/user')
        .set('Authorization', '0 ' + tokens[0])
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.username).to.equal(userData.users.valid[0].username);
          expect(res.body.payload.email).to.equal(userData.users.valid[0].email);
          expect(res.body.payload.groupIds).to.have.lengthOf(1);
          expect(res.body.payload.groupIds).to.include(groupId);
          expect(res.body.payload.userId).to.have.lengthOf(36).and.to.be.a('string');
          expect(res.body.payload.imageUrl).to.equal(userData.users.valid[0].imageUrl);
        });
      });
    });

    describe('with error', () => {
      it('should not create a new group due to referencing a not existing user', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createWrongUser)
        .then(res => {
          expectResponse.toBe409CreateGroupNonExistingUser(res, groupScenarios[0].createWrongUser.users[0]);
        });
      });

      it('should not create a new group due to duplicated users', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createDuplicatedUser)
        .then(res => {
          expectResponse.toBe400CreateGroupDuplicatedUsers(res);
        });
      });

      it('should not create a new group due to group without creator user', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createWithoutCreatorUser)
        .then(res => {
          expectResponse.toBe400CreateGroupMissingCreator(res);
        });
      });

      it('should not create a new group due to create a group without users', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createNullUsers)
        .then(res => {
          expectResponse.toBe400CreateGroupMissingCreator(res);
        });
      });

      it('should not create a new group due to invalid payload', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createInvalidPayload)
        .then(res => {
          expectResponse.toBe400InvalidRequestBody(res);
        });
      });

      it('should not create a new group due to wrong token', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + 'foobar')
        .send(groupScenarios[0].create)
        .then(res => {
          expectResponse.toBe401InvalidAuthToken(res);
        });
      });

      it('should not create a new group due to missing token', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '')
        .send(groupScenarios[0].create)
        .then(res => {
          expectResponse.toBe401InvalidFormatHeaderAuthorization(res);
        });
      });
    });
  });

  describe('Get Group', () => {
    let tokens = {};
    let groupId;

    before('register User 0 and 1', done => {
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
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
          .send(groupScenarios[0].create);
      }).then(res => {
        groupId = res.body.payload.groupId;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    describe('with success', () => {
      it('should get the group by user 0', () => {
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId)
        .set('Authorization', '0 ' + tokens[0])
        .then(function(res) {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.name).to.equal(groupScenarios[0].create.name);
          expect(res.body.payload.imageUrl).to.equal(groupScenarios[0].create.imageUrl);
          expect(res.body.payload.users).to.have.lengthOf(groupScenarios[0].create.users.length);
          expect(res.body.payload.users.map(val => val.username))
          .to.have.members([userData.users.valid[0].username, userData.users.valid[1].username]);
          expect(res.body.payload.users.map(val => val.email))
          .to.have.members([userData.users.valid[0].email, userData.users.valid[1].email]);
          expect(res.body.payload.transactions).to.be.empty;
          expect(res.body.payload.groupId).to.equal(groupId);
          expect(res.body.payload.createdAt).to.be.an('string').and.not.to.be.empty;
        });
      });

      it('should get the group by user 1', () => {
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId)
        .set('Authorization', '0 ' + tokens[1])
        .then(function(res) {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.name).to.equal(groupScenarios[0].create.name);
          expect(res.body.payload.imageUrl).to.equal(groupScenarios[0].create.imageUrl);
          expect(res.body.payload.users).to.have.lengthOf(groupScenarios[0].create.users.length);
          expect(res.body.payload.users.map(val => val.username))
          .to.have.members([userData.users.valid[0].username, userData.users.valid[1].username]);
          expect(res.body.payload.users.map(val => val.email))
          .to.have.members([userData.users.valid[0].email, userData.users.valid[1].email]);
          expect(res.body.payload.transactions).to.be.empty;
          expect(res.body.payload.groupId).to.equal(groupId);
          expect(res.body.payload.createdAt).to.be.an('string').and.not.to.be.empty;
        });
      });
    });

    describe('with error', () => {
      it('should not get the group due to unauthorized user', () => {
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId)
        .set('Authorization', '0 ' + tokens[2])
        .then(res => {
          expectResponse.toBe403GroupsUserIsNotMember(res);
        });
      });

      it('should not get the group due to a wrong groupId', () => {
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/fooBar-this-is-not-an-valid-groupId')
        .set('Authorization', '0 ' + tokens[0])
        .then(res => {
          expectResponse.toBe404GroupNotFound(res);
        });
      });
    });
  });
});
