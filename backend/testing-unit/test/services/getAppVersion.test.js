'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = chai.expect;

const appVersionService = require('../../../services/appVersion.service');

describe('Test service "getAppVersion"', function() {
  it('should get current app version', function() {
    const result = appVersionService.getAppVersion();
    expect(result).to.be.an('object');
    expect(result.name).to.equal('Backend');
    expect(result.version).to.have.lengthOf.above(0);
  });
});
