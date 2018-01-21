'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

describe('Get version', function() {
    this.timeout(5000); // How long to wait for a response (ms)

    it('should get current app version', function() {
      return chai.request(settings.host)
       .get(settings.url.version.base)
       .then(function(res) {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload.name).to.equal('Backend');
          expect(res.body.payload.version).to.equal(settings.version.current);
        });
    });
  });
