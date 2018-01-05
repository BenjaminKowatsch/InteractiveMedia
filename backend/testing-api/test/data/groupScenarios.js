'use strict';

const userData = require('./user.data.json');

const MINUTE = 60000;
const HOURE = 60 * MINUTE;
const DAY = 24 * HOURE;
const nowPlus = time => new Date(new Date().getTime() + time).toISOString();

module.exports = [
  // Secenario 0:
  // User 0 + 1
  {
    create: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: [userData.users.valid[0].email, userData.users.valid[1].email]
    },
    createWrongUser: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: ['wrong_user_0_email@bar.foo', userData.users.valid[1].email]
    },
    createDuplicatedUser: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: [userData.users.valid[0].email, userData.users.valid[0].email]
    },
    createWithoutCreatorUser: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: [userData.users.valid[1].email, userData.users.valid[2].email]
    },
    createNullUsers: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: []
    },
    createInvalidPayload: {
      imageUrl: null,
      users: ['wrong_user_0_email@bar.foo', userData.users.valid[1].email]
    },
  },
  // Secenario 2:
  {
    createGroup0: {
      name: 'test_gruppe_0',
      imageUrl: null,
      users: [userData.users.valid[0].email, userData.users.valid[1].email]
    },
    createGroup1: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: [userData.users.valid[0].email]
    },
  },
  // Secenario 2:
  // User 0 + 1 + 2
  {
    createGroup: {
      name: 'scenario_1_group',
      imageUrl: null,
      users: [userData.users.valid[0].email, userData.users.valid[1].email, userData.users.valid[2].email]
    },
    users: [userData.users.valid[0], userData.users.valid[1], userData.users.valid[2]],
    nonGroupUser: userData.users.valid[3],
    setTokenForUser: function(id, token) { module.exports[2].users[id].token = token;},
    setIdForUser: function(id, userId) { module.exports[2].users[id].userId = userId;},
    setTokenForNonGroupUser: function(token) { module.exports[2].nonGroupUser.token = token;},
    setIdForNonGroupUser: function(userId) { module.exports[2].nonGroupUser.userId = userId;},
    transactions: [
      {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          longitude: 9.131,
          latitude: 48.947
        },
        infoCreatedAt: nowPlus(1 * MINUTE),
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: 'even'
      },
      {
        amount: 6.6,
        infoName: 'Test transaction 1',
        infoLocation: {
          longitude: null,
          latitude: null
        },
        infoCreatedAt: nowPlus(3 * HOURE),
        infoImageUrl: null,
        //paidBy: User 1,
        split: 'even'
      },
      {
        amount: 3.3,
        infoName: 'Test transaction 2',
        infoLocation: {
          longitude: null,
          latitude: null
        },
        infoCreatedAt: nowPlus(12 * HOURE),
        infoImageUrl: null,
        //paidBy: User 2,
        split: 'even'
      },
      {
        amount: 12.9,
        infoName: 'Test transaction 3',
        infoLocation: {
          longitude: 9.660790,
          latitude: 48.258534
        },
        infoCreatedAt: nowPlus(1 * DAY + 2 * HOURE),
        infoImageUrl: null,
        //paidBy: User 0,
        split: 'even'
      },
      { //liegt zwischen  1 und 2
        amount: 9,
        infoName: 'Test transaction 4',
        infoLocation: {
          longitude: null,
          latitude: null
        },
        infoCreatedAt: nowPlus(5 * HOURE),
        infoImageUrl: null,
        //paidBy: User 1,
        split: 'even'
      },
      { //nicht durch 3 teilbar
        amount: 10,
        infoName: 'Test transaction 5',
        infoLocation: {
          longitude: null,
          latitude: null
        },
        infoCreatedAt: nowPlus(2 * DAY + 2 * HOURE),
        infoImageUrl: null,
        //paidBy:  User 2,
        split: 'even'
      },
      { // wie transaktion 4 aber anderer paidBy
        amount: 9,
        infoName: 'Test transaction 6',
        infoLocation: {
          longitude: null,
          latitude: null
        },
        infoCreatedAt: nowPlus(2 * DAY + 5 * HOURE),
        infoImageUrl: null,
        //paidBy: User 0,
        split: 'even'
      },
    ],
    transactionWrongUserId: {
      amount: 9,
      infoName: 'transactionWrongUserId',
      infoLocation: {
        longitude: null,
        latitude: null
      },
      infoCreatedAt: nowPlus(1 * DAY),
      infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
      paidBy: '6367e722-e857-4d0f-bf78-278a92260418',
      split: 'even'
    },
    transactionWrongLocation1: {
      amount: 6.6,
      infoName: 'Test transaction wrong location 1',
      infoLocation: {
        longitude: 50,
        latitude: null
      },
      infoCreatedAt: nowPlus(1 * DAY),
      infoImageUrl: null,
      //paidBy: User 1,
      split: 'even'
    },
    transactionWrongLocation2: {
      amount: 6.6,
      infoName: 'Test transaction wrong location 2',
      infoLocation: {
        longitude: 190,
        latitude: 50
      },
      infoCreatedAt: nowPlus(1 * DAY),
      infoImageUrl: null,
      //paidBy: User 1,
      split: 'even'
    },
    transactionWrongLocation3: {
      amount: 6.6,
      infoName: 'Test transaction wrong location 3',
      infoLocation: {
        longitude: 50,
        latitude: 100
      },
      infoCreatedAt: nowPlus(1 * DAY),
      infoImageUrl: null,
      //paidBy: User 1,
      split: 'even'
    },
    transactionWrongInfoCreatedAt: {
      amount: 6.6,
      infoName: 'Test transaction 1',
      infoLocation: {
        longitude: null,
        latitude: null
      },
      infoCreatedAt: nowPlus(-1 * HOURE),
      infoImageUrl: null,
      //paidBy: User 1,
      split: 'even'
    },
    setUserIdsInTransactions: function(users) {
      let transactions = module.exports[2].transactions;
      let self = module.exports[2];
      transactions[0].paidBy = users[0].userId;
      transactions[1].paidBy = users[1].userId;
      transactions[2].paidBy = users[2].userId;
      transactions[3].paidBy = users[0].userId;
      transactions[4].paidBy = users[1].userId;
      transactions[5].paidBy = users[2].userId;
      transactions[6].paidBy = users[0].userId;
      self.transactionWrongLocation1.paidBy = users[0].userId;
      self.transactionWrongLocation2.paidBy = users[0].userId;
      self.transactionWrongLocation3.paidBy = users[0].userId;
      self.transactionWrongInfoCreatedAt.paidBy = users[0].userId;

    }
  },
];
