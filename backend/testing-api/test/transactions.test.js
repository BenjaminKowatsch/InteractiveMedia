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

const registerUser = index => chai.request(HOST).post(URL.BASE_USER  + '/').send(userData.users.valid[index]);
const getUserData = token => chai.request(HOST).get(URL.BASE_USER  + '/user').set('Authorization', '0 ' + token);
const deepCopy = data => JSON.parse(JSON.stringify(data));

// ************* Tests ***********//
describe('Groups-Controller: Transactions:', () => {

  describe('Create transactions with error', () => {
    let scenario1GroupId;
    let groupId;
    let users = deepCopy(groupScenarios[2].users);
    let transactions = groupScenarios[2].transactions;

    before('register user 0, 1, 2, create two groups, prepare testData', function(done) {
      this.timeout(10000);
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
        users[0].token = res.body.payload.accessToken;
        return getUserData(users[0].token);
      }).then(res => {
        users[0].userId = res.body.payload.userId;
        return registerUser(1);
      }).then(res => {
        users[1].token = res.body.payload.accessToken;
        return getUserData(users[1].token);
      }).then(res => {
        users[1].userId = res.body.payload.userId;
        return registerUser(2);
      }).then(res => {
        users[2].token = res.body.payload.accessToken;
        return getUserData(users[2].token);
      }).then(res => {
        users[2].userId = res.body.payload.userId;
        return chai.request(HOST)
          .post(URL.BASE_GROUP  + '/')
          .set('Authorization', '0 ' + users[0].token)
          .send(groupScenarios[2].createGroup);
      }).then(res => {
        groupId = res.body.payload.groupId;
        groupScenarios[2].setUserIdsInTransactions(users);
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/')
        .set('Authorization', '0 ' + users[0].token)
        .send(groupScenarios[1].createGroup0); //only user 0 + 1
      }).then(res => {
        scenario1GroupId = res.body.payload.groupId;
        done();
      }).catch((error) => {
        console.log('Register User or Group Error: ' + error);
      });
    });

    it('should fail to add a transaction due to unknown groupId', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/wrong_group_id/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transactions[0])
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

    it('should fail to add a transaction due to unknown userId (paidBy)', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(groupScenarios[2].transactionWrongUserId)
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('authorization');
        expect(res.body.payload.message).to.equal('user is not but has to be a member of the group');
      });
    });

    it('should fail to add a transaction due to userId (paidBy) not in group', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + scenario1GroupId + '/transactions')
      .set('Authorization', '0 ' + users[2].token)
      .send(transactions[0])
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

    it('should fail to add a transaction due to inconsistent location data', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(groupScenarios[2].transactionWrongLocation1)
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

    it('should fail to add a transaction due to invalide longitude location data', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(groupScenarios[2].transactionWrongLocation2)
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

    it('should fail to add a transaction due to invalide latitude location data', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(groupScenarios[2].transactionWrongLocation3)
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

    it('should fail to add a transaction due to invalide info-created-at', () => {
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(groupScenarios[2].transactionWrongInfoCreatedAt)
      .then(res => {
        expect(res).to.have.status(400);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('transaction');
        expect(res.body.payload.message)
        .to.equal('invalide time adjustment: group.createdAt is gt transaction.infoCreatedAt');
      });
    });

    it('should fail to add a transaction due to missing amount', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.amount;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to missing infoName', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.infoName;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to missing infoLocation', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.infoLocation;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to missing infoCreatedAt', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.infoCreatedAt;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to missing infoImageUrl', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.infoImageUrl;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to missing paidBy', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.paidBy;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to missing split', () => {
      let transaction = deepCopy(transactions[0]);
      delete transaction.split;
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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

    it('should fail to add a transaction due to additional parameter', () => {
      let transaction = deepCopy(transactions[0]);
      transaction.publishedAt = 'fooBar';
      return chai.request(HOST)
      .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
      .set('Authorization', '0 ' + users[0].token)
      .send(transaction)
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
  });

  describe('Create and get transactions', function() {
    let groupId;
    let users = deepCopy(groupScenarios[2].users);
    let transactions = groupScenarios[2].transactions;

    before('register user 0, 1, 2, create group, prepare testData', function(done) {
      this.timeout(10000);
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
        users[0].token = res.body.payload.accessToken;
        return getUserData(users[0].token);
      }).then(res => {
        users[0].userId = res.body.payload.userId;
        return registerUser(1);
      }).then(res => {
        users[1].token = res.body.payload.accessToken;
        return getUserData(users[1].token);
      }).then(res => {
        users[1].userId = res.body.payload.userId;
        return registerUser(2);
      }).then(res => {
        users[2].token = res.body.payload.accessToken;
        return getUserData(users[2].token);
      }).then(res => {
        users[2].userId = res.body.payload.userId;
        return chai.request(HOST)
          .post(URL.BASE_GROUP  + '/')
          .set('Authorization', '0 ' + users[0].token)
          .send(groupScenarios[2].createGroup);
      }).then(res => {
        groupId = res.body.payload.groupId;
        groupScenarios[2].setUserIdsInTransactions(users);
        done();
      }).catch((error) => {
        console.log('Register User or Group Error: ' + error);
      });
    });

    describe('Scenario 2', () => {
      it('should add transaction 0 by user_0', () => {
        let transaction = transactions[0];
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(function(res) {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.amount).to.equal(transaction.amount);
          expect(res.body.payload.infoName).to.equal(transaction.infoName);
          expect(res.body.payload.infoLocation).to.deep.equal(transaction.infoLocation);
          expect(res.body.payload.infoCreatedAt).to.equal(transaction.infoCreatedAt);
          expect(res.body.payload.infoImageUrl).to.equal(transaction.infoImageUrl);
          expect(res.body.payload.paidBy).to.equal(transaction.paidBy);
          expect(res.body.payload.publishedAt).to.be.a('string').with.lengthOf(24);
        });
      });
    });
  });
});
