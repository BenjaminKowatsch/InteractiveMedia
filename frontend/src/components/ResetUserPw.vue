<!-- TEMPLATE for ResetUserPw.vue
  * Description: A form to reset the password of any "password" registered user to a default value ("interactive"). 
  You should not try to reset the password of facebook or google users
 -->
<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <span class="text-md-center">  
        <v-form v-model="valid" ref="form" lazy-validation>
          <v-text-field
            label="Email of user"
            v-model="email"
            @keyup.enter="resetPW()"    
          ></v-text-field>
          <v-tooltip top>
            <v-btn icon slot="activator">
              <v-icon>fa-question-circle</v-icon>
            </v-btn>
          <span>Passwordlogin only (Logintype 0): Reset user password to default "interactive" (for support purpose)</span>
          </v-tooltip>
           <v-btn
            @click="resetPW"
            :disabled="!valid"
          >
            Reset PW
          </v-btn>
          <v-btn @click="clear">Clear</v-btn>
      </span>
      <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="green darken-4"
          v-model="successReset"
        >
          Password resetted ("interactive")
          <v-btn dark flat @click.native="successReset = false">X</v-btn>
        </v-snackbar>
        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="passwordAlreadyResetted"
        >
          Password has already been resetted
          <v-btn dark flat @click.native="passwordAlreadyResetted = false">X</v-btn>
        </v-snackbar>
        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="noUserFound"
        >
          Email doesn't exists
        <v-btn dark flat @click.native="noUserFound = false">X</v-btn>
        </v-snackbar>
        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="errorReset"
        >
          Error in request
          <v-btn dark flat @click.native="errorReset = false">X</v-btn>
        </v-snackbar>
    </v-layout>
  </v-container>        
</template>

<script>

import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

 export default {
      props: ['authToken', 'users'],

    data () {
      return {
        email: '',
        userId: '',
        user: [],
        successReset: false,
        noUserFound: false,
        passwordAlreadyResetted: false,
        requestError: false
      }
    },

    mounted: function(){

    },

    methods: {

        // Resets the password of a existing user (via email) to a default value
        resetPW: function(){

            if(this.email.length > 0){

                this.user = this.users.filter(this.filter_userID)

                if(this.user.length > 0){

                    this.userId = this.user[0].userId
                    var sha256 = require('js-sha256')
                    var hashedPassword = sha256('interactive')

                    var reset = {
                        password: hashedPassword
                    }
                    console.log(hashedPassword)
                    console.log(reset)
                    axios.put(Config.webServiceURL + "/v1/admin/users/" + this.userId, reset, {
                    headers: { Authorization: "0 " + this.authToken }
                    })
                    .then(response => {
                        if(response.data.success == true) {
                            this.successReset = true
                            this.clear()
                        }
                    })
                    .catch(e => {
                        if(e.response.status == 500) {                          
                            this.passwordAlreadyResetted = true
                            this.clear()                       
                        }
                        else 
                        {
                            this.requestError = true
                            this.clear()
                        }
                        console.log("Errors reset pw: " + e);   
                    });
                }else 
                {
                  this.noUserFound = true
                  this.clear()
                }
            }

        },

        // Resets the form after update attempt or canceling 
        clear: function () {
            this.$refs.form.reset()
            this.email = '',
            this.user = [],
            this.userId = ''
        },

        // Used to get the userID from a given email to perform the update request
        filter_userID: function(users) {
        return users.email == this.email;
        },
    }
}
</script>


 
