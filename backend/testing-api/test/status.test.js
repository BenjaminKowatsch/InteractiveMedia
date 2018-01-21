'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

describe('Get status', function() {
    it('should get current status', function() {
      return chai.request(settings.host)
       .get(settings.url.status.base)
       .then(function(res) {
          expect(res).to.have.status(200);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.true;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.status).to.equal('healthy');
        });
    });
  });
