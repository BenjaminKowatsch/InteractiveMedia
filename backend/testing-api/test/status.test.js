/*jshint expr: true*/

var chai = require('chai');
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = 'http://backend:8081';
var baseUrl = '/v1/status';

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
