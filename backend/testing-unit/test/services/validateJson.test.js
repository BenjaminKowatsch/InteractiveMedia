'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const assert = chai.assert;
const expect = chai.expect;

const validateJsonService = require('../../../services/validateJson.service');

const jsonSchema = {
    registerUserPayload: require('../../../jsonSchema/registerUserPayload.json')
  };

const testData = {
  'userData': {
      'validInput': {
        'username': 'barack',
        'password': 'obama',
        'email': 'barack.obama@usa.gov',
        'imageUrl': null
      },
      'invalidInput': {
        'username': 'too',
        'password': 'short',
        'email': 'barack.obama@usa.gov',
        'imageUrl': null
      }
    }
};

describe('Test service "validateJson"', function() {
  it('should return true for valid input', function() {
    const result = validateJsonService.validateAgainstSchema(testData.userData.validInput,
      jsonSchema.registerUserPayload);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.true;
  });

  it('should return false for invalid input', function() {
    const result = validateJsonService.validateAgainstSchema(testData.userData.invalidInput,
      jsonSchema.registerUserPayload);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });

  it('should return false for null input', function() {
    const result = validateJsonService.validateAgainstSchema(null, jsonSchema.registerUserPayload);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });

  it('should return false for empty input', function() {
    const result = validateJsonService.validateAgainstSchema({}, jsonSchema.registerUserPayload);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });
});
