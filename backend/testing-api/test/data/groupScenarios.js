'use strict';

const userData = require('./user.data.json');

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
  },{
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
    setTokenForUser: function(id, token) { module.exports[1].users[id].token = token;},
    setIdForUser: function(id, userId) { module.exports[1].users[id].userId = userId;},
    setTokenForNonGroupUser: function(token) { module.exports[1].nonGroupUser.token = token;},
    setIdForNonGroupUser: function(userId) { module.exports[1].nonGroupUser.userId = userId;},
    transactions: [
      {
        amount: 9,
        infoName: 'Test transaction 0',
        infoLocation: {
          latitude: 48.947,
          longitude: 9.131
        },
        infoCreatedAt: '2017-11-01T18:00:00.000Z', // 01.11.2017, 18:00 Uhr
        infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
        //paidBy: User 0,
        split: 'even'
      },
      {
        amount: 6.6,
        infoName: 'Test transaction 1',
        infoLocation: null,
        infoCreatedAt: '2017-11-01T19:00:00.000Z', // 01.11.2017, 19:00 Uhr
        infoImageUrl: null,
        //paidBy: User 1,
        split: 'even'
      },
      {
        amount: 3.3,
        infoName: 'Test transaction 2',
        infoLocation: null,
        infoCreatedAt: '2017-11-02T16:00:00.000Z', // 02.11.2017, 16:00 Uhr
        infoImageUrl: null,
        //paidBy: User 2,
        split: 'even'
      },
      {
        amount: 12.9,
        infoName: 'Test transaction 3',
        infoLocation: {
          latitude: 48.258534,
          longitude: 9.660790
        },
        infoCreatedAt: '2017-11-02T23:30:00.000Z', // 02.11.2017, 23:30 Uhr
        infoImageUrl: null,
        //paidBy: User 0,
        split: 'even'
      },
      { //liegt zwischen  1 und 2
        amount: 9,
        infoName: 'Test transaction 4',
        infoLocation: null,
        infoCreatedAt: '2017-11-02T11:00:00.000Z', // 02.11.2017, 11:00 Uhr
        infoImageUrl: null,
        //paidBy: User 1,
        split: 'even'
      },
      { //nicht durch 3 teilbar
        amount: 10,
        infoName: 'Test transaction 5',
        infoLocation: null,
        infoCreatedAt: '2017-11-03T00:30:00.000Z', // 03.11.2017, 00:30 Uhr
        infoImageUrl: null,
        //paidBy:  User 2,
        split: 'even'
      },
      { // wie transaktion 4 aber anderer paidBy
        amount: 9,
        infoName: 'Test transaction 6',
        infoLocation: null,
        infoCreatedAt: '2017-11-02T11:00:00.000Z', // 02.11.2017, 11:00 Uhr
        infoImageUrl: null,
        //paidBy: User 0,
        split: 'even'
      },
    ],
    setUserIdsInTransactions: function() {
      let users =  module.exports[1].users;
      let transactions = module.exports[1].transactions;
      transactions[0].paidBy = users[0].userId;
      transactions[1].paidBy = users[1].userId;
      transactions[2].paidBy = users[2].userId;
      transactions[3].paidBy = users[0].userId;
      transactions[4].paidBy = users[1].userId;
      transactions[5].paidBy = users[2].userId;
      transactions[6].paidBy = users[0].userId;
    },
    transactionWrongUserId: {
      amount: 9,
      infoName: 'transactionWrongUserId',
      infoLocation: null,
      infoCreatedAt: '2017-04-23T18:25:43.511Z',
      infoImageUrl: '10896cb8-d2a4-4bb6-b4d7-c3063553fee9.image.jpg',
      paidBy: '6367e722-e857-4d0f-bf78-278a92260418',
      split: 'even'
    }
  },
];
