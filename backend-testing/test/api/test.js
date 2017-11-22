var chai = require('chai');  
var expect = require('chai').expect;

chai.use(require('chai-http'));

var host = "http://backend:8081";

var testData = {
  'users': [
    {
      'username': 'alex1',
      'password': 'alexpassword'
    }
  ]
};

describe('Dummy test', function() {  
 this.timeout(5000); // How long to wait for a response (ms)

 before(function() {});
 after(function() {});

 // GET - /hello/world
 it('should return message', function() {
   return chai.request(host)
     .get('/hello/world')
     .then(function(res) {
       expect(res).to.have.status(200);
       expect(res).to.be.json;
       expect(res.body).to.be.an('object');
       expect(res.body.status).to.equal('up');
     });
 });
});

describe('Register and login with username + password', function() {  
  this.timeout(5000); // How long to wait for a response (ms)
 
  before(function() {});
  after(function() {});

 // POST - Register new user
 it('should register new user', function() {
   return chai.request(host)
     .post('/register')
     .send({username: testData.users[0].username, password: testData.users[0].password})
     .then(function(res) {
       expect(res).to.have.status(200);
       expect(res).to.be.json;
       expect(res.body).to.be.an('object');
       expect(res.body.success).to.be.true;
       expect(res.body.payload).to.be.an('object');
       expect(res.body.payload.accessToken.length).to.equal(200);
       expect(res.body.payload.authType).to.equal(0);
     });
 });

  // POST - Register existing user
  it('should fail to register existing user', function() {
    return chai.request(host)
      .post('/register')
      .send({username: testData.users[0].username, password: testData.users[0].password})
      .then(function(res) {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.false;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.dataPath).to.equal('username');
        expect(res.body.payload.message).to.equal('Username already exists');
      });
  });

  // POST - Login
  it('should login', function() {
    return chai.request(host)
      .post('/launometer_login')
      .send({username: testData.users[0].username, password: testData.users[0].password})
      .then(function(res) {
        expect(res).to.have.status(200);
        expect(res).to.be.json;
        expect(res.body).to.be.an('object');
        expect(res.body.success).to.be.true;
        expect(res.body.payload).to.be.an('object');
        expect(res.body.payload.accessToken.length).to.equal(200);
        expect(res.body.payload.authType).to.equal(0);
      });
  });

});
