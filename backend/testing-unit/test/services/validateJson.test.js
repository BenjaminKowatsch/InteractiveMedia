'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const assert = chai.assert;
const expect = chai.expect;

const validateJsonService = require('../../../services/validateJson.service');

const jsonSchema = {
    userData: require('../../../JSONSchema/userData.json')
  };

const testData = {
  'userData': {
      'validInput': {
        'username': 'barack',
        'password': 'obama'
      },
      'invalidInput': {
        'username': 'too',
        'password': 'short'
      }
    }
};

describe('Test service "validateJson"', function() {
  it('should return true for valid input', function() {
    const result = validateJsonService.validateAgainstSchema(testData.userData.validInput, jsonSchema.userData);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.true;
  });

  it('should return false for invalid input', function() {
    const result = validateJsonService.validateAgainstSchema(testData.userData.invalidInput, jsonSchema.userData);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });

  it('should return false for null input', function() {
    const result = validateJsonService.validateAgainstSchema(null, jsonSchema.userData);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });

  it('should return false for empty input', function() {
    const result = validateJsonService.validateAgainstSchema({}, jsonSchema.userData);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });
});
