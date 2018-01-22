'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = chai.expect;
const chaiAsPromised = require('chai-as-promised');
const ERROR = require('../../../config/error.config');
const expectedResult = require('../../util/expectedResult.util');

chai.use(chaiAsPromised);

// test specific
const service = require('../../../services/token.service');
const testData = require('../../data/token.data');

describe.only('Service "token"', function() {
  describe('with success', function() {

    it('should generate correct auth token', function() {
      const expected = testData.valid.token;
      const result = service.generateAccessToken(testData.valid.input.payload, testData.valid.input.secret);
      return expect(result).to.deep.equal(expected);
    });

    it('should resolve with correct decoded data', function() {
      const expected = expectedResult.success.withPayload(testData.valid.input.payload);
      const result = service.decodeToken(testData.valid.token, testData.valid.input.secret);
      return expect(result).to.be.fulfilled.and.to.eventually.deep.equal(expected);
    });
  });

  describe('with error', function() {
    it('should reject to decode due to invalid auth token', function() {
      const expected = expectedResult.error.invalidAuthToken;
      const promise = service.decodeToken(testData.invalid.token, testData.valid.input.secret);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject to decode due to false secret', function() {
      const expected = expectedResult.error.invalidAuthToken;
      const promise = service.decodeToken(testData.valid.token, testData.invalid.input.secret);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject to decode due to missing payload and secret', function() {
      const expected = expectedResult.error.invalidAuthToken;
      const promise = service.decodeToken();
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject to decode due to missing secret', function() {
      const expected = expectedResult.error.invalidAuthToken;
      const promise = service.decodeToken(testData.valid.token);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });
  });
});
