'use strict';

/*jshint expr: true, node:true, mocha:true*/

var chai = require('chai');
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = 'http://backend:8081';
var baseUrl = '/v1/version';

describe('Get version', function() {
    this.timeout(5000); // How long to wait for a response (ms)

    before(function() {});
    after(function() {});

    // POST - Register new user
    it('should get current app version', function() {
      return chai.request(host)
       .get(baseUrl + '/')
       .then(function(res) {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.name).to.equal('Backend');
          expect(res.body.version).to.have.lengthOf.above(0);
        });
    });
  });
