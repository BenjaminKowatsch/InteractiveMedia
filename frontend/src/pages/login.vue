<!-- TEMPLATE for login screen
  * Name:        Login VUE
  * Author:      Benjamin Kowatsch
  * Comments:    Isabeau Schmidt-Nunez
  * Description: View for logging into the web app
 -->

<template>
<!-- START Framework 7 Template Elements for establishing the view -->
  <f7-page login-screen>
    <!-- Page Content -->
    <f7-login-screen-title>Login</f7-login-screen-title>
    <!-- title of the login view -->
    <f7-list form>
      <f7-list-item>
        <f7-label>Nutzername</f7-label>
        <!-- label for username entry textfield -->
        <f7-input name="username" placeholder="Nutzername" type="text" v-model="username" @keyup.enter="checkLogin()"></f7-input>
        <!-- input field for username -->
      </f7-list-item>
      <f7-list-item>
        <f7-label>Passwort</f7-label>
        <!-- label for password entry textfield -->
        <f7-input name="password" type="password" placeholder="Passwort" v-model="password" @keyup.enter="checkLogin()"></f7-input>
        <!-- input field for password -->
      </f7-list-item>
    </f7-list>
    <f7-list>
      <f7-list-button title="Einloggen" v-on:click="checkLogin()">
      <!-- Login Button which triggers the login function (see below in jscript area) -->

      </f7-list-button>
    </f7-list>
    <f7-list>
      <googlesigninbutton @success="googleOnSignIn" @error="this.defaultErrorHandler">
        Login with Google
      </googlesigninbutton>
      <!-- Login with Google button which triggers the login function via Google plus account (see below in jscript area) -->
    </f7-list>
    <f7-list>
      <facebooksigninbutton id="facebookLogin" :params="facebookparams" @success="facebookOnSignIn" @error="this.defaultErrorHandler">
        Login with Facebook
      </facebooksigninbutton>
      <!-- Login with facebook button which triggers the login function via facebook account (see below in jscript area) -->
    </f7-list>
    <hr>
    <div class="register">
      <f7-list>
        <f7-list-button title="Registrieren" v-on:click="redirect('/register', true, true, false)">
        <!-- register button which redirects to register page -->
        </f7-list-button>
      </f7-list>
    </div>

  </f7-page>
  <!-- END of Template Elements -->
</template>

<script>
/*
* START JScript Elements for establishing the view according to the template elements
* imports Facebooksigninbutton, Googlesigninbutton, jwt, axios, Cookie, Config
*/
  import Mixins from '../mixins.js'
  import Facebooksigninbutton from '@/components/facebooksigninbutton'
  import Googlesigninbutton from '@/components/googlesigninbutton'
  import axios from 'axios'
  import Cookie from '../js/Cookie.js'
  import Config from '../js/Config.js'

  export default {
    name: 'login',
    mixins: [ Mixins ],
    components: {
      Googlesigninbutton,
      Facebooksigninbutton
      // sets the signin buttons of Facebook and Google as Components to the View
    },
    data() {
      return {
        errors: [],
        username: '',
        password: '',
        login: '',
        facebookparams: {
          scope: 'email,user_likes',
          return_scopes: true
        }
        // sets the variables and defines them
      }
    },
    methods: {
      /**
       * Callback function which will be called when the facebook signin button component
       * executes the login request and receives the response from facebook.
       * This function receives the response from facebook including the accessToken for the current session.
       * This accessToken will be send to the backend for verification and logging the user in
       * @param  {JSONObject} response The response from facebook for the login request
       */
      facebookOnSignIn: function(response) {
        // Response Action on clicking the facebook signin button
          var access_token = response.authResponse.accessToken; // Send access token to backend
          axios.post(Config.webServiceURL+'/facebook_login', {
              "accessToken": access_token
            })
            .then(this.loginResponseHandler)
            .catch(this.defaultErrorHandler);
        },
        /**
         * Callback function which will be called when the google signin button component
         * executes the login request and receives the response from google.
         * This function receives the response from google including the accessToken for the current session.
         * This accessToken will be send to the backend for verification and logging the user in
         * @param  {Object} googleUser Object which contains the accessToken/idToken from google
         */
        googleOnSignIn: function(googleUser) {
          // Response action on clicking the google signin button
          var idToken = googleUser.getAuthResponse().id_token; // Send id token to backend
          axios.post(Config.webServiceURL+'/google_login', {
              "accessToken": idToken
            })
            .then(this.loginResponseHandler)
            .catch(this.defaultErrorHandler);
        },
        /**
         * Callback function for printing out the error response of a POST request
         * @param  {JSONObject} error Object which holds error information
         */
        defaultErrorHandler: function(error) {
          console.log("Error: "+JSON.stringify(error)); // logs an error to the console
        },
        /**
         * Callback function for handling the non error response of the login POST requests
         * @param  {JSONObject} response Containing the valid accessToken for the session and an authentication type to be stored into a cookie
         */
        loginResponseHandler: function(response) {
          this.checkServerResponse(response, payload =>{
            console.log('Correct'); // logs to console when the login data was correct with the database
            this.loginUser(payload); // response to the logged in user
          }, error =>{
            console.log("Wrong");
            // logs to the console when login data didn't match to any database entry
            this.$f7.alert('Username oder Passwort falsch', 'Launometer');
          });
        },
        /**
         * This function sends a POST request with the user credentials to the backend.
         * If a non error response was received the loginResponseHandler will be called and the user will be logged in.
         */
        checkLogin: function() {
          if(this.username !== '' && this.password !== ''){
            // function to check the login data
            var credentials = {
              "username": this.username,
              "password": this.password
              // sets the variables and defines them as the tiped in data
            };
            // Sending credentials to database
            axios.post(Config.webServiceURL+'/launometer_login', credentials)
            .then(this.loginResponseHandler)
            .catch(this.defaultErrorHandler);
          } else {
            this.$f7.alert('Username oder Passwort darf nicht leer sein', 'Launometer');
          }
        },
        reset: function() {
          this.username = '';
          this.password = '';
        }
      }
    }

</script>
