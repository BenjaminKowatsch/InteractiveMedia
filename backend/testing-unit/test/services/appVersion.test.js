'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = chai.expect;

const expectedResult = require('../../util/expectedResult.util');

// test specific
const service = require('../../../services/appVersion.service');

describe('Service "appVersion"', function() {
  it('should get current app version', function() {
    const expected = expectedResult.success.appVersion;
    const result = service.getAppVersion();
    expect(result).to.deep.equal(expected);
  });
});
