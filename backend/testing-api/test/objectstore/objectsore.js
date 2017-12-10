/* jshint expr: true */

var chai = require('chai');
var fs = require('fs');
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = 'http://backend:8081';
var baseUrl = '/v1/object-store';

describe('Upload and download images', function() {
  var imageData;

  before(function(done) {
    imageData = fs.readFileSync('image.png');
    console.log('imageData size: ' + imageData.size);
    done();
  });

  it('should upload a new image', function() {
    return chai.request(host)
            .post(baseUrl + '/upload?filename=image.png')
            .attach('uploadField', imageData, 'image.png')
            .then(res => {
              console.log(JSON.stringify(res));
            });
  });

  it('should download an existing image', function() {
    return chai.request(host)
            .get(baseUrl + '/download?filename=image.png')
            .then(res => {
              console.log('Image comparison: ' + (imageData === res.text));
              console.log(JSON.stringify(res));
            });
  });
});
