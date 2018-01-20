'use strict';

/*jshint expr: true, node:true, mocha:true*/

const chai = require('chai');
const expect = require('chai').expect;

chai.use(require('chai-http'));
const expectResponse = require('../util/expectResponse.util');

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
          expectResponse.toBe404.urlNotFound(res);
        });
    });

    it('should return an error for unknown POST endpoint', function() {
      return chai.request(url.host)
       .post(url.base + url.unknownRoute)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });

    it('should return an error for unknown DELETE endpoint', function() {
      return chai.request(url.host)
       .delete(url.base + url.unknownRoute)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });

    it('should return an error for unknown PUT endpoint', function() {
      return chai.request(url.host)
       .put(url.base + url.unknownRoute)
       .then(function(res) {
          expectResponse.toBe404.urlNotFound(res);
        });
    });
  });
