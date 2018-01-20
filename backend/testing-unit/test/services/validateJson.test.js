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

describe('Test service "validateJson"', function() {});
