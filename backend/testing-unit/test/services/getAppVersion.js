/*jshint expr: true*/

var chai = require('chai');
var expect = chai.expect;

var appVersion = require('../../../services/getAppVersion');

describe('Test service "getAppVersion"', function() {
  it('should get current app version', function() {
    var result = appVersion.getAppVersion();
    expect(result).to.be.an('object');
    expect(result.name).to.equal('Backend');
    expect(result.version).to.have.lengthOf.above(0);
  });
});
