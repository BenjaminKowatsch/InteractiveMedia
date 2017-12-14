const userData = require('./user.data.json');

module.exports = [
  // Secenario 1:
  // User 0 + 1
  {
    create: {
      name: 'test_gruppe_1',
      imageUrl: null,
      users: [userData.users.valid[0].email, userData.users.valid[1].email]
    }

  }
];
