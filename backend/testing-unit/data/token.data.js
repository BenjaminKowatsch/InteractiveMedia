'use strict';

module.exports = {
  valid: {
    input: {
      payload: {
        userId: 'AAAAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA',
        expiryDate: new Date('December 31, 2018 22:22:22').toISOString()
      },
      secret: 'thisIsARandomGeneratedSecret'
    },
    token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.' +
      'eyJ1c2VySWQiOiJBQUFBQUFBQS1BQUFBLUFBQUEtQUFBQS1BQUFBQUFBQUFBQUEiLCJleHBpcnlEYXRlIjoiMj' +
      'AxOC0xMi0zMVQyMjoyMjoyMi4wMDBaIn0.sE6M3c9kG8PcD2GmMYVLAOvQqVNamygO2G29PKG6UD8',
  },
  invalid: {
    input: {
      secret: 'anotherUnknownSecret',
    },
    token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.' +
      'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX' +
      'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.sE6M3c9kG8PcD2GmMYVLAOvQqVNamygO2G29PKG6UD8',
  }
};
