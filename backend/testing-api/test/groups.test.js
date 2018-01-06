'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseHelper = require('./data/databaseHelper');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  BASE_GROUP: '/v1/groups',
  BASE_USER: '/v1/users',
};

const userData = require('./data/user.data');
const groupScenarios = require('./data/groupScenarios');

// ************* Helper ***********//

const registerUser = index => chai.request(HOST).post(URL.BASE_USER  + '/').send({
  username: userData.users.valid[index].username,
  email: userData.users.valid[index].email,
  password: userData.users.valid[index].password
});

describe('Groups-Controller', () => {
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
          expect(res).to.have.status(409);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('groupUsers');
          expect(res.body.payload.message).to.equal('Unknown user: ' + groupScenarios[0].createWrongUser.users[0]);
        });
      });

      it('should not create a new group due to duplicated users', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createDuplicatedUser)
        .then(res => {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('groupUsers');
          expect(res.body.payload.message).to.equal('Duplicated groupUsers');
        });
      });

      it('should not create a new group due to group without creator user', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createWithoutCreatorUser)
        .then(res => {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('groupUsers');
          expect(res.body.payload.message).to.equal('GroupCreator must be part of groupUsers');
        });
      });

      it('should not create a new group due to create a group without users', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createNullUsers)
        .then(res => {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('groupUsers');
          expect(res.body.payload.message).to.equal('GroupCreator must be part of groupUsers');
        });
      });

      it('should not create a new group due to invalid payload', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + tokens[0])
        .send(groupScenarios[0].createInvalidPayload)
        .then(res => {
          expect(res).to.have.status(400);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('validation');
          expect(res.body.payload.message).to.equal('Invalid body');
        });
      });

      it('should not create a new group due to wrong token', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + 'foobar')
        .send(groupScenarios[0].create)
        .then(res => {
          expect(res).to.have.status(401);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('authentication');
          expect(res.body.payload.message).to.equal('invalid authToken');
        });
      });

      it('should not create a new group due to missing token', () => {
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '')
        .send(groupScenarios[0].create)
        .then(res => {
          expect(res).to.have.status(401);
          expect(res).to.be.json;
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('authentication');
          expect(res.body.payload.message).to.
            equal('invalid number of arguments provided in http request header Authorization');
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
          expect(res).to.have.status(403);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('authorization');
          expect(res.body.payload.message).to.equal('user is not but has to be a member of the group');
        });
      });

      it('should not get the group due to a wrong groupId', () => {
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/fooBar-this-is-not-an-valid-groupId')
        .set('Authorization', '0 ' + tokens[0])
        .then(res => {
          expect(res).to.have.status(404);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.dataPath).to.equal('group');
          expect(res.body.payload.message).to.equal('group not found');
        });
      });
    });
  });
});

/***************************** Maxis Shizzle *******************************/
/*   it.skip('should respond with 403 if all groups are accessed as nonAdmin', () => {
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
 */
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

  it.skip('should deny access to users not in group', () => {
    return chai.request(HOST).
        get(baseUrl + '/group').
        send({'accessToken': alternativeToken, 'authType': 0}).
        then(function(res) {
          expect(res).to.have.status(403);
        });
  });
*/
