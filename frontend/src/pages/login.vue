<!-- TEMPLATE for login view
  * Description: View for logging into the web app
 -->
<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <v-form ref="form" lazy-validation > 
        <v-text-field
          append-icon="account_circle"
          label="Username"
          v-model="username"
          :rules="usernameRules"
          @keyup.enter="checkLogin()"
          required
        ></v-text-field>
        <v-text-field
          append-icon="lock"
          label="Password"
          v-model="password"
          :rules="passwordRules"
          type="password"
          @keyup.enter="checkLogin()"    
          required
        ></v-text-field>
        <v-btn
          @click="checkLogin"
          :disabled="!valid"
        >    
          Login
        </v-btn>
        <v-btn @click="reset">Reset</v-btn>

        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="alertAccessDenied"
        >
          Access denied
          <v-btn dark flat @click.native="alertAccessDenied = false">X</v-btn>
        </v-snackbar>

        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="alertWrongCredentials"
        >
          Wrong credentials
          <v-btn dark flat @click.native="alertWrongCredentials = false">X</v-btn>       
        </v-snackbar>

        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="alertUnknownRole"
        >
          Unknown role of user
          <v-btn dark flat @click.native="alertUnknownRole = false">X</v-btn>
        </v-snackbar>
      </v-form>
    </v-layout>
  </v-container>
</template>
 
<script>
/*
* START JScript Elements for establishing the view according to the template elements
* imports jwt, axios, Cookie, Config
*/
import Mixins from "../mixins.js";

import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

export default {
  name: "login",
  mixins: [Mixins],
  components: {},
  data: () => ({
      drawer: false,
      valid: true,
      errors: [],
      username: "",
      usernameRules: [
        (v) => !!v || 'Username is required',
      ],
      password: "",
      passwordRules: [
        (v) => !!v || 'Password is required',
      ],
      email: "",
      login: "",
      alertAccessDenied: false,
      alertWrongCredentials: false,
      alertUnknownRole: false,
      // sets the variables and defines them
  }),
  methods: {
    /*     registerDummyUser: function() {
      axios
        .post(Config.webServiceURL + "/v1/users", {
          username: "yxcvbn",
          email: "yxcvb@web.de",
          password: "yxcvb"
        })
        .then(function(response) {
          console.log(response);
        })
        .catch(function(error) {
          console.log(error);
        });
    }, */

    /**
         * Callback function for printing out the error response of a POST request
         * @param  {JSONObject} error Object which holds error information
         */
    defaultErrorHandler: function(error) {
      console.log("Error: " + JSON.stringify(error)); // logs an error to the console
      //If logindata doenst match a db entry, show error and reload page
     /*  this.$f7.alert("Login failed", "Debts² Admin Panel", () => { */
        this.alertWrongCredentials = true
        this.redirect("/", false, false, false);
        console.log("redirecting to /")
      /* }); */
    },
    /**
         * Callback function for handling the non error response of the login POST requests
         * @param  {JSONObject} response Containing the valid accessToken for the session and an authentication type to be stored into a cookie
         */
    loginResponseHandler: function(response) {
      //read authToken for getUser request
      var authToken = response.data.payload.accessToken;
      var userDataRole = "";
      //get userData from requested login
      axios
        .get(Config.webServiceURL + "/v1/users/user", {
          headers: { Authorization: `0 ${authToken}` }
        })
        .then(responseAxios => {
          this.userDataRole = responseAxios.data.payload.role;
          //check role for admin and send informations to create a cookie and redirect admin to overview page
          console.log(this.userDataRole)
          if (this.userDataRole === "admin") {
            this.altertLoggedIn = true;
            this.checkServerResponse(response, payload => {
              console.log("Login corret: Role 'admin'"); // logs to console when the login data was correct with the database
              this.loginUser(payload);
            });
            //handling other roles
          } else if (this.userDataRole === "user") {
              this.alertAccessDenied = true
          } else {
               this.alertUnknownRole = true
         }
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors userrequest: " + e);
          if(e.toString() =="Error: Request failed with status code 401"){
            this.alertAccessDenied = true
          }
        });
    },
    /**
         * This function sends a POST request with the user credentials to the backend.
         * If a non error response was received the loginResponseHandler will be called and the user will be logged in.
         */
    checkLogin: function() {
        if (this.$refs.form.validate()){
      /* if (this.username !== "" && this.password !== "") { */
        // function to check the login data
        var credentials = {
          username: this.username,
          password: this.password
          // sets the variables and defines them as the tiped in data
        };
        // Sending credentials to database
        //mocking!!!!!
        /* /          this.loginResponseHandler({
              "data":{
                "success": true,
                "payload": {
                  "accessToken": "this_is_the_token_lol",
                  "authType": 0
                },
              "status": 201
              }
            });  */
        axios
          .post(Config.webServiceURL + "/v1/users/login?type=0", credentials)
          .then(this.loginResponseHandler)
          .catch(this.defaultErrorHandler);
      } /* else {
        this.$f7.alert(
          "Username oder Passwort darf nicht leer sein",
          "Debts² Admin Panel"
        );
      } */
    },
    reset: function() {
        this.$refs.form.reset()
    }
  }
};
</script>
