/* jshint expr: true */

const chai = require('chai');
const fs = require('fs');
const expect = require('chai').expect;
const databaseHelper = require('../data/databaseHelper');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  BASE_GROUP: '/v1/groups/',
  REGISTER_USER: '/v1/users/',
  BASE_OBJECTSTORE: '/v1/object-store'
};
const userData = require('../data/user.data');

var registerUser = index => chai.request(HOST).post(URL.REGISTER_USER).send({
  username: userData.users.valid[index].username,
  email: userData.users.valid[index].email,
  password: userData.users.valid[index].password
});

describe('Object-store', function() {

  describe('Upload image', function() {
    let imageData;
    let token;
    before('load image to upload', function(done) {
      imageData = fs.readFileSync('image.png');
      done();
    });
    before('register User 0', done => {
      databaseHelper.promiseResetDB().then(()=> {
        return registerUser(0);
      }).then(res => {
        token = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should upload a new image', function() {
      return chai.request(HOST)
        .post(URL.BASE_OBJECTSTORE + '/upload')
        .attach('uploadField', imageData, 'image.png')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(201);
          expect(res.body.success).to.be.true;
          expect(res.body.path).to.not.be.null;
        });
    });

    it('should fail upload a new image with invalid auth token', function() {
      return chai.request(HOST)
        .post(URL.BASE_OBJECTSTORE + '/upload')
        .attach('uploadField', imageData, 'image.png')
        .set('Authorization', '0 ' + 'XXX')
        .then(res => {
          expect(res).to.have.status(401);
          expect(res.body.success).to.be.false;
          expect(res.body.payload.dataPath).to.be.equal('authtoken');
          expect(res.body.payload.message).to.be.equal('invalid auth token');
        });
    });

  });

  describe('Download image', function() {
      let imageData;
      let token;
      let imagePath;
      before('load image to upload', function(done) {
        imageData = fs.readFileSync('image.png');
        done();
      });
      before('register User 0', done => {
        databaseHelper.promiseResetDB().then(()=> {
          return registerUser(0);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      before('upload a new image', function() {
        return chai.request(HOST)
          .post(URL.BASE_OBJECTSTORE + '/upload')
          .attach('uploadField', imageData, 'imageToDownload.png')
          .set('Authorization', '0 ' + token)
          .then(res => {
            imagePath = res.body.payload.path;
          });
      });

      it('should download an existing image', function() {
        return chai.request(HOST)
          .get(URL.BASE_OBJECTSTORE + '/download?filename=' + imagePath)
          .set('Authorization', '0 ' + token)
          .then(res => {
            expect(res).to.have.status(200);
          });
      });

      it('should fail to download an existing image with invalid auth token', function() {
        return chai.request(HOST)
          .get(URL.BASE_OBJECTSTORE + '/download?filename=' + imagePath)
          .set('Authorization', '0 ' + 'XXX')
          .then(res => {
            expect(res).to.have.status(401);
            expect(res.body.success).to.be.false;
            expect(res.body.payload.dataPath).to.be.equal('authtoken');
            expect(res.body.payload.message).to.be.equal('invalid auth token');
          });
      });

      it('should fail to download a missing image', function() {
        return chai.request(HOST)
          .get(URL.BASE_OBJECTSTORE + '/download?filename=missingimage.png')
          .set('Authorization', '0 ' + token)
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
