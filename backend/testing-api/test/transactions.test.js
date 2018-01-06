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
const wait = seconds => new Promise(resolve => setTimeout(resolve, seconds));
const expectTransaction = (given, expected) => {
  expect(given).to.be.an('object');
  expect(given.infoName).to.equal(expected.infoName);
  expect(given.amount).to.equal(expected.amount);
  expect(given.infoLocation).to.deep.equal(expected.infoLocation);
  expect(given.infoCreatedAt).to.equal(expected.infoCreatedAt);
  expect(given.infoImageUrl).to.equal(expected.infoImageUrl);
  expect(given.paidBy).to.equal(expected.paidBy);
  expect(given.publishedAt).to.be.a('string').with.lengthOf(24);
};

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
    let groupCreatedAt;
    let users = deepCopy(groupScenarios[2].users);
    users[0].localTransactions = [];
    users[1].localTransactions = [];
    users[2].localTransactions = [];
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
        groupCreatedAt = res.body.payload.createdAt;
        groupId = res.body.payload.groupId;
        groupScenarios[2].setUserIdsInTransactions(users);
        done();
      }).catch((error) => {
        console.log('Register User or Group Error: ' + error);
      });
    });

    describe('Scenario 2', () => {
      it('should add transaction_0 by user_0', () => {
        let transaction = transactions[0];
        return chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          users[0].localTransactions.push(res.body.payload);
        });
      });

      it('should pull for new transactions(t_0) by user_1', () => {
        let transaction = transactions[0];
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + groupCreatedAt)
        .set('Authorization', '0 ' + users[1].token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          users[1].localTransactions.push(res.body.payload[0]);
        });
      });

      it('should pull for new transactions(t_0) by user_2', () => {
        let transaction = transactions[0];
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + groupCreatedAt)
        .set('Authorization', '0 ' + users[2].token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          users[2].localTransactions.push(res.body.payload[0]);
        });
      });

      it('should add transaction_1 by user_1', () => {
        let transaction = transactions[1];
        return wait(2000).then(() => chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[1].token)
        .send(transaction))
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          users[1].localTransactions.push(res.body.payload);
        });
      });

      it('should pull for new transactions(t_1) by user_0', () => {
        let transaction = transactions[1];
        let user = users[0];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should pull for new transactions(t_1) by user_2', () => {
        let transaction = transactions[1];
        let user = users[2];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should add transaction_3 by user_2', () => {
        let transaction = transactions[3];
        let user = users[2];
        return wait(2000).then(() => chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + user.token)
        .send(transaction))
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          user.localTransactions.push(res.body.payload);
        });
      });

      it('should pull for new transactions(t_3) by user_1', () => {
        let transaction = transactions[3];
        let user = users[1];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should add transaction_2 by user_1', () => {
        let transaction = transactions[2];
        let user = users[1];
        return wait(2000).then(() => chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + user.token)
        .send(transaction))
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          user.localTransactions.push(res.body.payload);
        });
      });

      it('should pull for new transactions(t_2) by user_2', () => {
        let transaction = transactions[2];
        let user = users[2];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should pull for new transactions(t_2, t_3) by user_0', () => {
        let transaction2 = transactions[2];
        let transaction3 = transactions[3];
        let user = users[0];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(2);
          //transactions are sorted by server by pubhlishedAt -> 2 before 3
          expectTransaction(res.body.payload[0], transaction3);
          user.localTransactions.push(res.body.payload[0]);
          expectTransaction(res.body.payload[1], transaction2);
          user.localTransactions.push(res.body.payload[1]);
        });
      });

      it('should add transaction_4 by user_0', () => {
        let transaction = transactions[4];
        let user = users[0];
        return wait(2000).then(() => chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + user.token)
        .send(transaction))
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          user.localTransactions.push(res.body.payload);
        });
      });

      it('should pull for new transactions(t_4) by user_1', () => {
        let transaction = transactions[4];
        let user = users[1];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should pull for new transactions(t_4) by user_2', () => {
        let transaction = transactions[4];
        let user = users[2];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should add transaction_5 by user_2', () => {
        let transaction = transactions[5];
        let user = users[2];
        return wait(2000).then(() => chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + user.token)
        .send(transaction))
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          user.localTransactions.push(res.body.payload);
        });
      });
      it('should pull for new transactions(t_5) by user_0', () => {
        let transaction = transactions[5];
        let user = users[0];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should pull for new transactions(t_5) by user_1', () => {
        let transaction = transactions[5];
        let user = users[1];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
        });
      });

      it('should add transaction_6 by user_0', () => {
        let transaction = transactions[6];
        let user = users[0];
        return wait(2000).then(() => chai.request(HOST)
        .post(URL.BASE_GROUP  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + user.token)
        .send(transaction))
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expectTransaction(res.body.payload, transaction);
          user.localTransactions.push(res.body.payload);
          expect(user.localTransactions).to.have.a.lengthOf(7);
        });
      });
      it('should request new transactions(t_6) by user_1', () => {
        let transaction = transactions[6];
        let user = users[1];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
          expect(user.localTransactions).to.have.a.lengthOf(7);
        });
      });
      it('should request new transactions(t_6) by user_2', () => {
        let transaction = transactions[6];
        let user = users[2];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          expectTransaction(res.body.payload[0], transaction);
          user.localTransactions.push(res.body.payload[0]);
          expect(user.localTransactions).to.have.a.lengthOf(7);
        });
      });
      it('should request new transactions(none) by user_0', () => {
        let user = users[0];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(0);
        });
      });
      it('should request new transactions(none) by user_1', () => {
        let user = users[1];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(0);
        });
      });
      it('should request new transactions(none) by user_2', () => {
        let user = users[2];
        let lastTransactionDate = user.localTransactions[user.localTransactions.length - 1].publishedAt;
        return chai.request(HOST)
        .get(URL.BASE_GROUP  + '/' + groupId + '/transactions?after=' + lastTransactionDate)
        .set('Authorization', '0 ' + user.token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(0);
        });
      });
    });
  });
});
