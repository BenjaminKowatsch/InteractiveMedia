'use strict';

const userData = require('./user.data.json');
const SPLIT = require('../config/split.config');
const miscService = require('../util/miscService.util');

const MINUTE = 60000;
const HOUR = 60 * MINUTE;
const DAY = 24 * HOUR;

module.exports = {
  constantDeduction: {
    valid: [
      {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          longitude: 9.131,
          latitude: 48.947
        },
        infoCreatedAt: miscService.nowPlus(1 * MINUTE),
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: [
          {
            type: SPLIT.CONSTANT_DEDUCTION,
            amount: 5,
            // userid: User 0
          }
        ]
      }
    ],
    invalid: {
      base: {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          longitude: 9.131,
          latitude: 48.947
        },
        infoCreatedAt: miscService.nowPlus(1 * MINUTE),
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: [
          {
            type: SPLIT.CONSTANT_DEDUCTION,
            amount: 5,
            // userid: User 0
          }
        ]
      },
      unknownUserId: {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          longitude: 9.131,
          latitude: 48.947
        },
        infoCreatedAt: miscService.nowPlus(1 * MINUTE),
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: [
          {
            type: SPLIT.CONSTANT_DEDUCTION,
            amount: 5,
            userId: 'XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX'
          }
        ]
      },
    },
    setUserIdsInTransactions: function(users) {
      const self = module.exports.constantDeduction;
      self.valid[0].paidBy = users[0].userId;
      self.valid[0].split[0].userId = users[0].userId;
      self.invalid.base.paidBy = users[0].userId;
      self.invalid.base.split[0].userId = users[0].userId;
      self.invalid.unknownUserId.paidBy = users[0].userId;
    }
  },
  combination: {
    valid: {
      constantDeduction_even: {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          longitude: 9.131,
          latitude: 48.947
        },
        infoCreatedAt: miscService.nowPlus(1 * MINUTE),
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: [
          {
            type: SPLIT.CONSTANT_DEDUCTION,
            amount: 5,
            // userid: User 0
          },
          {
            type: SPLIT.EVEN
          }
        ]
      },
    },
    invalid: {
      even_constantDeduction: {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          longitude: 9.131,
          latitude: 48.947
        },
        infoCreatedAt: miscService.nowPlus(1 * MINUTE),
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: [
          {
            type: SPLIT.EVEN
          },
          {
            type: SPLIT.CONSTANT_DEDUCTION,
            amount: 5,
            // userid: User 0
          },
        ]
      },
    },
    setUserIdsInTransactions: function(users) {
      const self = module.exports.combination;
      self.valid.constantDeduction_even.paidBy = users[0].userId;
      self.valid.constantDeduction_even.split[0].userId = users[0].userId;
      self.invalid.even_constantDeduction.paidBy = users[0].userId;
      self.invalid.even_constantDeduction.split[1].userId = users[0].userId;
    }
  }
};
