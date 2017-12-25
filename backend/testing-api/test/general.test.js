/*jshint expr: true*/

const chai = require('chai');
const expect = require('chai').expect;

chai.use(require('chai-http'));

const url = {
    'host': 'http://backend:8081',
    'base': '/v1',
    'unknownRoute': '/unknown'
  };

describe('Unknown endpoint', function() {
    it('should return an error for unknown GET endpoint', function() {
      return chai.request(url.host)
       .get(url.base + url.unknownRoute)
       .then(function(res) {
          expect(res).to.have.status(404);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.message).to.equal('Not found');
          expect(res.body.payload.dataPath).to.equal('GET ' + url.base + url.unknownRoute);
        });
    });

    it('should return an error for unknown POST endpoint', function() {
      return chai.request(url.host)
       .post(url.base + url.unknownRoute)
       .then(function(res) {
          expect(res).to.have.status(404);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.message).to.equal('Not found');
          expect(res.body.payload.dataPath).to.equal('POST ' + url.base + url.unknownRoute);
        });
    });

    it('should return an error for unknown DELETE endpoint', function() {
      return chai.request(url.host)
       .delete(url.base + url.unknownRoute)
       .then(function(res) {
          expect(res).to.have.status(404);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.message).to.equal('Not found');
          expect(res.body.payload.dataPath).to.equal('DELETE ' + url.base + url.unknownRoute);
        });
    });

    it('should return an error for unknown PUT endpoint', function() {
      return chai.request(url.host)
       .put(url.base + url.unknownRoute)
       .then(function(res) {
          expect(res).to.have.status(404);
          expect(res).to.be.json;
          expect(res.body).to.be.an('object');
          expect(res.body.success).to.be.false;
          expect(res.body.payload).to.be.an('object');
          expect(res.body.payload.message).to.equal('Not found');
          expect(res.body.payload.dataPath).to.equal('PUT ' + url.base + url.unknownRoute);
        });
    });
  });
