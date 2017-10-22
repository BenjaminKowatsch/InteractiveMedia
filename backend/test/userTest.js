var expect = require('chai').expect;
var helper = require('./helper');
var assert = require('assert');
var user = require('../modules/user');



describe("Test user register function", function () {

    it("Test if registered User is saved in database", function (done) {

        helper.up(function (collection) {

            var responseData = {};

            user.register(collection, responseData, 'Test', 'test')
                .then(function (res) {

                    // Assertion
                    expect(res.payload.accessToken.length).to.equal(200);
                    helper.down(collection);
                    done();
                   
                }).catch(function (error) {
                    console.log(error);
                    done();

                });    
        });
    });


    it("Test if login works", function (done) {

        helper.up(function (collection) {

            var responseData = {};

            var userData = {
                expiryDate: "2017-06-19 22:08:47.976Z",
                password: "test",
                username: "Test",
                userId: "1588313e-0c4a-43d2-8e8f-33fca9dfaece"
            };

            // Register a test user 

            collection.insertOne(userData, function (err, r) {
                assert.equal(null, err);
                assert.equal(1, r.insertedCount);
            });
            done();
            user.launometerLogin(collection, responseData, 'Test', 'test').
            then(function (res) {
                expect(res.payload.accessToken.length).to.equal(200);
                helper.down(collection);
                done();
            });      
        });
    });

});