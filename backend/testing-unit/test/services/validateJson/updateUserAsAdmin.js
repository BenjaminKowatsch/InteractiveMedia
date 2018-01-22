'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = chai.expect;
const chaiAsPromised = require('chai-as-promised');
const ERROR = require('../../../../config/error.config');
const expectedResult = require('../../../util/expectedResult.util');

chai.use(chaiAsPromised);

// test specific
const service = require('../../../../services/validateJson.service');
const jsonSchema = require('../../../../jsonSchema/updateUserAsAdminPayload.json');
const testData = require('../../../data/validateJson.data').updateUserAsAdminPayload;

describe('Service "validateJson" for "updateUserAsAdminPayload"', function() {
  describe('with success', function() {
    const expected = expectedResult.success.emptyPayload;

    it('should resolve for valid input with all attributes', function() {
      const promise = service.reqBodyAgainstSchema(testData.valid.allFields, jsonSchema);
      return expect(promise).to.be.fulfilled.and.to.eventually.deep.equal(expected);
    });

    it('should resolve for valid input with one attribute', function() {
      const promise = service.reqBodyAgainstSchema(testData.valid.oneFieldUsername, jsonSchema);
      return expect(promise).to.be.fulfilled.and.to.eventually.deep.equal(expected);
    });
  });

  describe('with error', function() {
    const expected = expectedResult.error.invalidRequestBody;

    it('should reject due to empty payload', function() {
      const promise = service.reqBodyAgainstSchema({}, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to additional property "userId"', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateUserId, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to additional property "groupIds"', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateGroupIds, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to additional property "_id"', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateInternalId, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to additional property "authType"', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateAuthType, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to additional unknown property', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateUnknownField, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to invalid role', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateInvalidRole, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to null username', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateUsernameNull, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to null password', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updatePasswordNull, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });

    it('should reject due to null email', function() {
      const promise = service.reqBodyAgainstSchema(testData.invalid.updateEmailNull, jsonSchema);
      return expect(promise).to.be.rejected.and.to.eventually.deep.equal(expected);
    });
  });
});
