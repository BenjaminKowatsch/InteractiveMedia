'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = chai.expect;

// test specific
const service = require('../../../services/email.service');

describe('Service "email"', function() {
  it('should generate correct artifical email address of facebook user from username', function() {
    const userId = 1234567890;
    const username = 'Harry Hirsch';
    const expected = 'harry_hirsch.90@fb.debtsquared';
    const result = service.generateFacebookEmailFromUsername(userId, username);
    expect(result).to.equal(expected);
  });
});
