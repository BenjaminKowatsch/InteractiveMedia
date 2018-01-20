'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const fs = require('fs');
const expect = require('chai').expect;
const databaseHelper = require('./data/databaseHelper');
const expectResponse = require('../util/expectResponse.util');

chai.use(require('chai-http'));

const HOST = 'http://backend:8081';

const URL = {
  BASE_GROUP: '/v1/groups/',
  REGISTER_USER: '/v1/users/',
  BASE_OBJECTSTORE: '/v1/object-store'
};
const userData = require('./data/user.data');

const registerUser = index => chai.request(HOST).post(URL.REGISTER_USER).send(userData.users.valid[index]);

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
          expectResponse.toBe401.invalidAuthToken(res);
        });
    });

    it('should fail upload a new image with missing uploadField', function() {
      return chai.request(HOST)
        .post(URL.BASE_OBJECTSTORE + '/upload')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expectResponse.toBe400.invalidRequestBody(res);
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
            expectResponse.toBe401.invalidAuthToken(res);
          });
      });

      it('should fail to download a missing image', function() {
        return chai.request(HOST)
          .get(URL.BASE_OBJECTSTORE + '/download?filename=missingimage.png')
          .set('Authorization', '0 ' + token)
          .then(res => {
            expectResponse.toBe404.fileNotFound(res);
          });
      });

      it('should fail to download with missing filename url parameter', function() {
        return chai.request(HOST)
          .get(URL.BASE_OBJECTSTORE + '/download')
          .set('Authorization', '0 ' + token)
          .then(res => {
            expectResponse.toBe400.objectstore.missingUrlParameterFilename(res);
          });
      });
    });
});
