/* jshint expr: true */

var chai = require('chai');
var fs = require('fs');
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = 'http://backend:8081';
var baseUrl = '/v1/object-store';

describe.only('Object-store', function() {

  describe('Upload image', function() {
    var imageData;
    before(function(done) {
      imageData = fs.readFileSync('image.png');
      done();
    });

    it('should upload a new image', function() {
      return chai.request(host)
        .post(baseUrl + '/upload?filename=image.png')
        .attach('uploadField', imageData, 'image.png')
        .then(res => {
          expect(res).to.have.status(201);
          expect(res.body.success).to.be.true;
        });
    });

  });

  describe('Download image', function() {
      it('should download an existing image', function() {
        return chai.request(host)
          .get(baseUrl + '/download?filename=image.png')
          .then(res => {
            expect(res).to.have.status(200);
          });
      });

      it('should fail to download a missing image', function() {
        return chai.request(host)
          .get(baseUrl + '/download?filename=missingimage.png')
          .then(res => {
            expect(res).to.have.status(500);
            expect(res).to.be.json;
            expect(res.body).to.be.an('object');
            expect(res.body.success).to.be.false;
            expect(res.body.payload).to.be.an('object');
            expect(res.body.payload.dataPath).to.equal('getObject');
            expect(res.body.payload.message).to.equal('failed to get object');
          });
      });
    });
});
