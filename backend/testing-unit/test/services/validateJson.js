/*jshint expr: true*/

var chai = require('chai');
var assert = chai.assert;
var expect = chai.expect;

var jsonValidator = require('../../../services/validateJson');

var jsonSchema = {
    userData: require('../../../JSONSchema/userData.json')
  };

var testData = {
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
    var result = jsonValidator.validateAgainstSchema(testData.userData.validInput, jsonSchema.userData);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.true;
  });

  it('should return false for invalid input', function() {
    var result = jsonValidator.validateAgainstSchema(testData.userData.invalidInput, jsonSchema.userData);
    expect(result).to.be.an('object');
    expect(result.valid).to.be.false;
  });
});