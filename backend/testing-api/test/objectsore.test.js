'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const fs = require('fs');
const expect = require('chai').expect;
const databaseHelper = require('./data/databaseHelper');
const expectResponse = require('../util/expectResponse.util');
const settings = require('../config/settings.config');
const userService = require('../util/userService.util');

chai.use(require('chai-http'));

const userData = require('./data/user.data');

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
        return userService.register(userData.users.valid[0]);
      }).then(res => {
        token = res.body.payload.accessToken;
        done();
      }).catch((error) => {
        console.log('Register User Error: ' + error);
      });
    });

    it('should upload a new image', function() {
      return chai.request(settings.host)
        .post(settings.url.objectstore.base + '/upload')
        .attach('uploadField', imageData, 'image.png')
        .set('Authorization', '0 ' + token)
        .then(res => {
          expect(res).to.have.status(201);
          expect(res.body.success).to.be.true;
          expect(res.body.path).to.not.be.null;
        });
    });

    it('should fail upload a new image with invalid auth token', function() {
      return chai.request(settings.host)
        .post(settings.url.objectstore.base + '/upload')
        .attach('uploadField', imageData, 'image.png')
        .set('Authorization', '0 ' + 'XXX')
        .then(res => {
          expectResponse.toBe401.invalidAuthToken(res);
        });
    });

    it('should fail upload a new image with missing uploadField', function() {
      return chai.request(settings.host)
        .post(settings.url.objectstore.base + '/upload')
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
          return userService.register(userData.users.valid[0]);
        }).then(res => {
          token = res.body.payload.accessToken;
          done();
        }).catch((error) => {
          console.log('Register User Error: ' + error);
        });
      });

      before('upload a new image', function() {
        return chai.request(settings.host)
          .post(settings.url.objectstore.base + '/upload')
          .attach('uploadField', imageData, 'imageToDownload.png')
          .set('Authorization', '0 ' + token)
          .then(res => {
            imagePath = res.body.payload.path;
          });
      });

      it('should download an existing image', function() {
        return chai.request(settings.host)
          .get(settings.url.objectstore.base + '/download?filename=' + imagePath)
          .set('Authorization', '0 ' + token)
          .then(res => {
            expect(res).to.have.status(200);
          });
      });

      it('should fail to download an existing image with invalid auth token', function() {
        return chai.request(settings.host)
          .get(settings.url.objectstore.base + '/download?filename=' + imagePath)
          .set('Authorization', '0 ' + 'XXX')
          .then(res => {
            expectResponse.toBe401.invalidAuthToken(res);
          });
      });

      it('should fail to download a missing image', function() {
        return chai.request(settings.host)
          .get(settings.url.objectstore.base + '/download?filename=missingimage.png')
          .set('Authorization', '0 ' + token)
          .then(res => {
            expectResponse.toBe404.fileNotFound(res);
          });
      });

      it('should fail to download with missing filename url parameter', function() {
        return chai.request(settings.host)
          .get(settings.url.objectstore.base + '/download')
          .set('Authorization', '0 ' + token)
          .then(res => {
            expectResponse.toBe400.objectstore.missingUrlParameterFilename(res);
          });
      });
    });
});
