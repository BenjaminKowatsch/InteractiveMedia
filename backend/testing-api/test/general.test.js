'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;
const expectResponse = require('../util/expectResponse.util');
const settings = require('../config/settings.config');

chai.use(require('chai-http'));

describe('Unknown endpoint', function() {
    it('should return an error for unknown GET endpoint', function() {
      return chai.request(settings.host)
       .get(settings.url.base + settings.url.unknown)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });

    it('should return an error for unknown POST endpoint', function() {
      return chai.request(settings.host)
       .post(settings.url.base + settings.url.unknown)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });

    it('should return an error for unknown DELETE endpoint', function() {
      return chai.request(settings.host)
       .delete(settings.url.base + settings.url.unknown)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });

    it('should return an error for unknown PUT endpoint', function() {
      return chai.request(settings.host)
       .put(settings.url.base + settings.url.unknown)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });
  });
