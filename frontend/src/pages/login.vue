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
        <f7-label>Username</f7-label>
        <!-- label for username entry textfield -->
        <f7-input name="username" placeholder="Username" type="text" v-model="username" @keyup.enter="checkLogin()"></f7-input>
        <!-- input field for username -->
      </f7-list-item>
      <f7-list-item>
        <f7-label>Password</f7-label>
        <!-- label for password entry textfield -->
        <f7-input name="password" type="password" placeholder="Password" v-model="password" @keyup.enter="checkLogin()"></f7-input>
        <!-- input field for password -->
      </f7-list-item>
    </f7-list>
    <f7-list>
      <f7-list-button title="Login" v-on:click="checkLogin()">
      <!-- Login Button which triggers the login function (see below in jscript area) -->

      </f7-list-button>
    </f7-list>
   

  </f7-page>
  <!-- END of Template Elements -->
</template>

<script>
/*
* START JScript Elements for establishing the view according to the template elements
* imports jwt, axios, Cookie, Config
*/
  import Mixins from '../mixins.js'

  import axios from 'axios'
  import Cookie from '../js/Cookie.js'
  import Config from '../js/Config.js'

  export default {
    name: 'login',
    mixins: [ Mixins ],
    components: {

    },
    data() {
      return {
        errors: [],
        username: '',
        password: '',
        login: '',
        // sets the variables and defines them
      }
    },
    methods: {
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
          console.log('loginResponseHandler');
          console.log(response);
          this.checkServerResponse(response, payload =>{
            console.log('Correct'); // logs to console when the login data was correct with the database
            this.loginUser(payload); // response to the logged in user
          }, error =>{
            console.log("Wrong");
            // logs to the console when login data didn't match to any database entry
            this.$f7.alert('Username oder Passwort falsch', 'Debts² Admin Panel');
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
            
            //mocking!!!!!
            this.loginResponseHandler({
              "data":{
                "success": true,
                "payload": {
                  "accessToken": "this_is_the_token_lol",
                  "authType": 0
                },
              "status": 201
              }
            });
            /* axios.post(Config.webServiceURL+'/v1/users/login?type=0', credentials)
            .then(this.loginResponseHandler)
            .catch(this.defaultErrorHandler); */
          } else {
            this.$f7.alert('Username oder Passwort darf nicht leer sein', 'Debts² Admin Panel');
          }
        },
        reset: function() {
          this.username = '';
          this.password = '';
        }
      }
    }

</script>
