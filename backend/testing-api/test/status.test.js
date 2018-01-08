'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;

chai.use(require('chai-http'));

const host = 'http://backend:8081';
const baseUrl = '/v1/status';

describe('Get status', function() {
    it('should get current status', function() {
      return chai.request(host)
       .get(baseUrl + '/')
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
