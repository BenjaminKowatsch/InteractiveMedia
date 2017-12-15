const userData = require('./user.data.json');

module.exports = [
  // Secenario 1:
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
    createInvalidePayload: {
      imageUrl: null,
      users: ['wrong_user_0_email@bar.foo', userData.users.valid[1].email]
    },
  }
];
