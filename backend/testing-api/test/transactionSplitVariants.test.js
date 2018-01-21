'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const winston = require('winston');
const databaseService = require('../util/databaseService');
const expectResponse = require('../util/expectResponse.util');
const settings = require('../config/settings.config');
const userService = require('../util/userService.util');
const groupService = require('../util/groupService.util');
const miscService = require('../util/miscService.util');

chai.use(require('chai-http'));

const userData = require('../data/user.data');
const groupScenarios = require('../data/groupScenarios');
const splitVariants = require('../data/transactionSplitVariants.data');

describe('Groups-Controller: Transactions: Split variants', () => {
  describe('constant deduction', () => {
    describe('with error', () => {
      let users = [miscService.deepCopy(userData.users.valid[0]), miscService.deepCopy(userData.users.valid[1])];
      let groupId;

      before('register user 0, 1, create group', function(done) {
        this.timeout(10000);
        databaseService.promiseResetDB().then(()=> {
          return userService.register(userData.users.valid[0]);
        }).then(res => {
          users[0].token = res.body.payload.accessToken;
          return userService.getUserData(0, users[0].token);
        }).then(res => {
          users[0].userId = res.body.payload.userId;
          return userService.register(userData.users.valid[1]);
        }).then(res => {
          users[1].token = res.body.payload.accessToken;
          return userService.getUserData(0, users[1].token);
        }).then(res => {
          users[1].userId = res.body.payload.userId;
          return groupService.create(0, users[0].token, groupScenarios[0].create);
        }).then(res => {
          groupId = res.body.payload.groupId;
          // prepare data: insert paidBy and split.userId
          splitVariants.constantDeduction.setUserIdsInTransactions(users);
          splitVariants.combination.setUserIdsInTransactions(users);
          done();
        }).catch((error) => {
          console.log('Register User or Group Error: ' + error);
        });
      });

      it('should fail to add a transaction due to missing amount in split_0', () => {
        let transaction = miscService.deepCopy(splitVariants.constantDeduction.invalid.base);
        delete transaction.split[0].amount;
        return chai.request(settings.host)
        .post(settings.url.groups.base  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to add a transaction due to missing userId in split_0', () => {
        let transaction = miscService.deepCopy(splitVariants.constantDeduction.invalid.base);
        delete transaction.split[0].userId;
        return chai.request(settings.host)
        .post(settings.url.groups.base  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
        });
      });

      it('should fail to add a transaction due to unknown userId in split_0', () => {
        let transaction = miscService.deepCopy(splitVariants.constantDeduction.invalid.unknownUserId);
        return chai.request(settings.host)
        .post(settings.url.groups.base  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expectResponse.toBe400.transactions.userIsNotMember(res);
        });
      });

      it('should fail to add a transaction because split of type "even" has a successor', () => {
        let transaction = miscService.deepCopy(splitVariants.combination.invalid.even_constantDeduction);
        return chai.request(settings.host)
        .post(settings.url.groups.base  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expectResponse.toBe400.transactions.invalidSplit(res);
        });
      });

    });

    describe('with success', () => {
      let users = [miscService.deepCopy(userData.users.valid[0]), miscService.deepCopy(userData.users.valid[1])];
      let groupId;
      let groupCreatedAt;

      before('register user 0, 1, create group', function(done) {
        this.timeout(10000);
        databaseService.promiseResetDB().then(()=> {
          return userService.register(userData.users.valid[0]);
        }).then(res => {
          users[0].token = res.body.payload.accessToken;
          return userService.getUserData(0, users[0].token);
        }).then(res => {
          users[0].userId = res.body.payload.userId;
          return userService.register(userData.users.valid[1]);
        }).then(res => {
          users[1].token = res.body.payload.accessToken;
          return userService.getUserData(0, users[1].token);
        }).then(res => {
          users[1].userId = res.body.payload.userId;
          return groupService.create(0, users[0].token, groupScenarios[0].create);
        }).then(res => {
          groupCreatedAt = res.body.payload.createdAt;
          groupId = res.body.payload.groupId;
          // prepare data: insert paidBy and split.userId
          splitVariants.constantDeduction.setUserIdsInTransactions(users);
          splitVariants.combination.setUserIdsInTransactions(users);
          done();
        }).catch((error) => {
          console.log('Register User or Group Error: ' + error);
        });
      });

      it('should add transaction_0 by user_0', () => {
        let transaction = splitVariants.constantDeduction.valid[0];
        return chai.request(settings.host)
        .post(settings.url.groups.base  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          groupService.expectTransaction(res.body.payload, transaction);
        });
      });

      it('should get transaction_0 by user_1', () => {
        let transaction = splitVariants.constantDeduction.valid[0];
        return chai.request(settings.host)
        .get(settings.url.groups.base  + '/' + groupId + '/transactions?after=' + groupCreatedAt)
        .set('Authorization', '0 ' + users[1].token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(1);
          groupService.expectTransaction(res.body.payload[0], transaction);
        });
      });

      it('should add transaction_1 with two splits by user_0', () => {
        let transaction = splitVariants.combination.valid.constantDeduction_even;
        return chai.request(settings.host)
        .post(settings.url.groups.base  + '/' + groupId + '/transactions')
        .set('Authorization', '0 ' + users[0].token)
        .send(transaction)
        .then(res => {
          expect(res).to.have.status(201);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          groupService.expectTransaction(res.body.payload, transaction);
        });
      });

      it('should get transaction_1 with two splits by user_1', () => {
        let transaction = splitVariants.combination.valid.constantDeduction_even;
        return chai.request(settings.host)
        .get(settings.url.groups.base  + '/' + groupId + '/transactions?after=' + groupCreatedAt)
        .set('Authorization', '0 ' + users[1].token)
        .then(res => {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('array').with.lengthOf(2);
          groupService.expectTransaction(res.body.payload[1], transaction);
        });
      });
    });
  });
});
