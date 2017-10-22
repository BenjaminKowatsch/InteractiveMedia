<!-- TEMPLATE for registration view
  * Name:        Overview VUE
  * Author:      Marc Rüttler
  * Comments:    Isabeau Schmidt-Nunez
  * Description: View to register a new user to the database
 -->

<template>
<!-- START Framework 7 Template Elements for establishing the view -->
            <f7-page id="register">
             <f7-navbar>
             <!-- navigation -->
                <f7-nav-left back-link="Back" sliding></f7-nav-left>
                <f7-nav-center sliding>Launometer</f7-nav-center>
                 <!-- label with app name -->
            </f7-navbar>
            <!-- Page Content -->
              <f7-login-screen-title>Registrieren</f7-login-screen-title>
              <!-- label for registering process -->
              <f7-list form>
                <!-- Form for data input -->
                <f7-list-item>
                    <f7-label>Username</f7-label>
                    <f7-input type="text" placeholder="Username" v-model='username' required="true" minlength="5" @keyup.enter="registerUser()" />
                    <!-- input field for username -->
                </f7-list-item>
                <f7-list-item v-if=" undefined !== errorMessages.username" >
                  <span style="color:red;" >{{ errorMessages.username }}</span>
                </f7-list-item>
                <!-- Password -->
                <f7-list-item>
                    <f7-label>Passwort</f7-label>
                    <f7-input type="password" placeholder="Passwort" v-model='password' required="true" minlength="5" @keyup.enter="registerUser()" />
                    <!-- input field for password -->
                </f7-list-item>
                <f7-list-item v-if=" undefined !== errorMessages.password" >
                  <span style="color:red;" >{{ errorMessages.password }}</span>
                </f7-list-item>
                <!-- Password -->
                <f7-list-item>
                    <f7-label>Passwort wdh.</f7-label>
                    <f7-input type="password" placeholder="Passwort wiederholen" v-model='password_wdh' required="true" minlength="5" @keyup.enter="registerUser()" />
                    <!-- input field for repeating password -->
                </f7-list-item>
              </f7-list>
              <f7-list>
                  <f7-list-button title="Registrieren" v-on:click="registerUser()" >
                  <!-- register button -->
                  </f7-list-button>
              </f7-list>
          </f7-page>
  <!-- END of Template Elements -->
</template>


<script>
 /*
  * START JScript Elements for establishing the view according to the template elements
  * imports Config, Cookie, axios
  */
import Mixins from '../mixins.js'
import Config from '../js/Config.js'
import axios from 'axios';

export default {
    name: 'login',
    mixins: [ Mixins ],
    data: function(){
        return {
          inputRequirements:{
            username: {
              minLength: 5,
              maxLength: 255,
            },
            password: {
              minLength: 5,
              maxLength: 255,
            }
          },
          errors: [],
           errorMessages: {
             username : undefined,
             password : undefined
             // sets the variables for error messages
           },
           username: "",
           password: '',
           password_wdh: '',
           login: ''
           // sets the variables and defines them
        }
    },
    methods:{
          /**
          * Callback function which will be called when the user wnats to register at launometer.
          * This function validates the users input and sends it to the backend. If the validation
          * fails a error message is shown
          */
        registerUser: function(){
          // Test POST function
          if(true === this.clientInputValidation()){
            // checks whether password and password repeated match
            var user = {"username": this.username, "password":this.password};
            console.log(JSON.stringify(user));
            // logs the user to the console
            axios.post(Config.webServiceURL+`/register`, user)
            .then(response => {
                console.log(JSON.stringify(response.data));
                this.errorMessages.username = "";
                this.errorMessages.password = "";
                this.checkServerResponse(response, payload =>{
                  this.loginUser(payload);
                }, error =>{
                  // Double security: If clientInputValidation is omitted and invalid data is send to the backend
                  // The backend will answer with error messages, which will be presented to the user
                  // Set error messages from server
                  this.errorMessages[response.data.dataPath] = response.data.message;
                });
            })
            .catch(e => {
                this.errors.push(e)
            });
          }
        },
         /**
          * This function is used to validate the user input against pre-defined rules. If the valiadtion fails, a error message is assigned to 
          * the errorMessages attribute
          */
        clientInputValidation: function(){

          if(this.username.length < this.inputRequirements.username.minLength){
            this.errorMessages.username = "The username is too short, minimum "+this.inputRequirements.username.minLength+" characteres."
            return false
          } else if(this.username.length > this.inputRequirements.username.maxLength){
            this.errorMessages.username = "The username is too long, maximum "+this.inputRequirements.username.maxLength+" characteres."
            return false
          } else{
            this.errorMessages.username = undefined
          }

          if(this.password.length < this.inputRequirements.password.minLength){
            this.errorMessages.password = "The password is too short, minimum "+this.inputRequirements.password.minLength+" characteres."
            return false
          } else if(this.password.length > this.inputRequirements.password.maxLength){
            this.errorMessages.password = "The password is too long, maximum "+this.inputRequirements.password.maxLength+" characteres."
            return false
          } else if(this.password !== this.password_wdh){
            this.errorMessages.password = "The entered passwords do not match."
            return false
          } else {
            this.errorMessages.password = undefined
          }
          return true
        }
  }
}
</script>
