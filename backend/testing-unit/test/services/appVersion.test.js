'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = chai.expect;

const expectResponse = require('../../util/expectResponse.util');

// test specific
const service = require('../../../services/appVersion.service');

describe('Service "appVersion"', function() {
  it('should get current app version', function() {
    const expected = expectResponse.success.appVersion;
    const result = service.getAppVersion();
    expect(result).to.deep.equal(expected);
  });
});
