/*jshint expr: true*/

const chai = require('chai');
const expect = require('chai').expect;

chai.use(require('chai-http'));

const host = 'http://backend:8081';
const baseUrl = '/v1/groups';

describe('Get version', function() {
    this.timeout(5000); // How long to wait for a response (ms)

    before(function() {});
    after(function() {});

    // GET - Register new user
    it('should get current app version', function() {
        return chai.request(host)
            .get(baseUrl + '/')
            .then(function(res) {
                expect(res).to.have.status(404);
                expect(res).to.be.json;
                expect(res.body).to.be.an('object');
                expect(res.body.name).to.equal('Backend');
                expect(res.body.version).to.have.lengthOf.above(0);
              });
      });
  });
