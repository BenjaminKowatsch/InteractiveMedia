<!-- TEMPLATE for adminProfile view
  * Description: Let the current logged in admin change specific data like his username, password, email and imageUrl.
                 There multiple or single props allowed
 -->
<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <span class="text-md-center">  
        <img src="../assets/logo.png"  alt="Debts² logo" class="logo">
        <h1 v-if="hasData">Welcome back, {{adminUserData.username}}!</h1>
        <h3 v-if="hasData">Current email: {{adminUserData.email}}</h3>
        <br/>
        <v-form v-model="valid" ref="form" lazy-validation>
          <v-text-field
            label="Username"
            v-model="username"
            @keyup.enter="checkUpdate()"    
          ></v-text-field>
          <v-text-field
            label="Email"
            v-model="email"
            @keyup.enter="checkUpdate()"    
          ></v-text-field>
          <v-text-field
            label="Password"
            v-model="password"
            type="password"
            @keyup.enter="checkUpdate()"    
          ></v-text-field>
          <v-text-field
            label="Retype password"
            v-model="passwordRe"
            type="password"
            @keyup.enter="checkUpdate()"    
          ></v-text-field>
          <v-text-field label="ImageUrl" @click='pickFile' v-model='imageName' prepend-icon='attach_file'></v-text-field>
					<input
						type="file"
						style="display: none"
						ref="image"
						accept="image/*"
          	@change="checkImageFile"
            @keyup.enter="checkUpdate()"    

					>
           <v-tooltip left>
            <v-btn icon slot="activator">
              <v-icon>fa-question-circle</v-icon>
            </v-btn>
            <span>Update single or multiple admin properties</span>
          </v-tooltip>
          <v-btn
            @click="checkUpdate"
            :disabled="!valid"
          >
            Update
          </v-btn>
          <v-btn @click="clear">Clear</v-btn>
        </v-form>
        <v-btn :to="{path:'/overview'}">Back</v-btn>
        </span>
        <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="yellow darken-4"
          v-model="passwordMatching"
        >
          Passwords not matching
          <v-btn dark flat @click.native="passwordMatching = false">X</v-btn>
        </v-snackbar>
                <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="green darken-4"
          v-model="successUpdate"
        >
          Successfully updatet
          <v-btn dark flat @click.native="successUpdate = false">X</v-btn>
        </v-snackbar>
                <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="red darken-4"
          v-model="errorUpdate"
        >
        Update error
          <v-btn dark flat @click.native="errorUpdata = false">X</v-btn>
        </v-snackbar>
    </v-layout>
  </v-container>
          
</template>

<script>

import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

  export default {

    data () {
      return {
        authToken: "",
        adminUserData: [],
        imageFileUrl: '',
        imageName: '',
        imageUrl: '',
        hasData: false,
        valid: true,
        username: '',
        email: '',
        password: '',
        passwordRe: '',
        select: null,
        passwordMatching: false,
        successUpdate: false,
        errorUpdate: false,
        data: []  
      }
    },

    mounted: function(){

      this.passwordRe = ''
      this.password = ''
      this.getOwnUser()  

    },

    methods: {

      // Is called if a image file is going to be picked in the form
      pickFile () {
        this.$refs.image.click ()
      },
 
      // Is called if a image file is picked to process them and write the file into a formdata
      // Later on, the formdata will be send to the object store to store the image
      checkImageFile (e) {
        const files = e.target.files
        if(files[0] !== undefined) {
          this.imageName = files[0].name
          if(this.imageName.lastIndexOf('.') <= 0) {
            return
          }
          const fr = new FileReader ()
          fr.readAsDataURL(files[0])
          fr.addEventListener('load', () => {

            this.data = new FormData();   
            let file = files[0];
            this.data.append('uploadField', file, file.name);
          })
        } else {
          this.imageName = ''
          this.imageUrl = ''
        }
      },
 
      // Get the own user details to display them on the admin profile page
      getOwnUser: function(){

        this.authToken = Cookie.getJSONCookie("accessToken").accessToken;

        axios
        .get(Config.webServiceURL + "/v1/users/user", {
           headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {
            this.adminUserData = response.data.payload
            this.hasData = true
        })
        .catch(e => {              
              console.log("Errors own user request (UserTableVuetify): " + e);           
            });
        },
      
      // Is called when a image is picked and the update button is pushed
      // Sends the image file to the object store and calls another method to update 
      // the users imageUrl. This image will then be shown in the android app as profile picture
      uploadImage: function (data){
      
        var upload = {
            uploadField : data
        }
          
        axios
          .post(Config.webServiceURL + "/v1/object-store/upload", data, {
          headers: { Authorization: "0 " + this.authToken, 'content-type': 'multipart/form-data' }
          })
          .then(response => {
            this.imageFileUrl = response.data.payload.path,
            this.updateImageUrl(this.imageFileUrl)          
          })
          .catch(e => {
            console.log("Errors in POST Upload: " + e);
            console.log(e)
          });       
      },
  
      // Updates the users imageUrl after a new image was uploaded to the object store
      updateImageUrl: function(imageFileUrl){

        var createdImageUrl = Config.webServiceURL + "/v1/object-store/download?filename=" + imageFileUrl
        console.log("ImageURL to update users informations: ")
        console.log(createdImageUrl)

        var userUpdateImage = {
          imageUrl: createdImageUrl
        }
        axios.put(Config.webServiceURL + "/v1/users/user", userUpdateImage, {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => 
          this.clear(),
          this.successUpdate = true
        )
        .catch(e => {
          console.log("Errors update imageUrl: " + e);
        });
      },

      // If the update button is pushed, check for all the desired updates and give the update informations
      // to the uploadImage() and update() methods
      checkUpdate: function () {

        if (this.$refs.form.validate()) {

          if(this.imageName.length > 0)
          {
            this.uploadImage(this.data)
          }

          if(this.username.length > 0 && this.password.length == 0 && this.email.length == 0)
          {
            var updateField = {
              username: this.username
            }
            this.update(updateField)
          }

          else if(this.username.length > 0 && this.password.length > 0 && this.email.length == 0)
          {
            if(this.password == this.passwordRe){

              var sha256 = require('js-sha256')
              var hashedPassword = sha256(this.password)
              var updateField = {
                username: this.username,
                password: hashedPassword
              }
              this.update(updateField)
            } 
            else
            {
            this.passwordMatching = true
            }
          }

          else if(this.username.length > 0 && this.password.length > 0 && this.email.length > 0)
          {
            if(this.password == this.passwordRe){

              var sha256 = require('js-sha256')
              var hashedPassword = sha256(this.password)
              var updateField = {
                username: this.username,
                password: hashedPassword,
                email: this.email
              }
              this.update(updateField)
            }
            else
            {
            this.passwordMatching = true
            }
          }

          else if(this.username.length > 0 && this.password.length == 0 && this.email.length > 0)
          {
              var updateField = {
                username: this.username,
                email: this.email
              }
              this.update(updateField)
          }

          else if(this.username.length == 0 && this.password.length > 0 && this.email.length > 0)
          {
            if(this.password == this.passwordRe)
            {
              var sha256 = require('js-sha256')
              var hashedPassword = sha256(this.password)
              var updateField = {
                password: hashedPassword,
                email: this.email
              }
              this.update(updateField)
            }
            else
            {
              this.passwordMatching = true
            }
          }

          else if(this.username.length == 0 && this.password.length > 0 && this.email.length == 0)
          {
            if(this.password == this.passwordRe)
            {
              var sha256 = require('js-sha256')
              var hashedPassword = sha256(this.password)
              var updateField = {
                password: hashedPassword
              }
              this.update(updateField)
            }
            else
            {
              this.passwordMatching = true
            }
          }

          else if(this.username.length == 0 && this.password.length == 0 && this.email.length > 0)
          {
              var updateField = {
                email: this.email,
              }
              this.update(updateField)            
          }
        }      
      },

      // Handles the provided informations from the checkUpdate() method and send a request to the
      // backend to update the desired user properties
      update: function(field){

        axios.put(Config.webServiceURL + "/v1/users/user", field, {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {
          if(response.data.success == true){
            this.successUpdate = true
            this.clear()
            this.getOwnUser()
          }
          else{
            this.errorUpdate = true
            this.clear()
          }
        })             
        .catch(e => {
          console.log("Errors update admin: " + e);
          this.errorUpdate = true
          this.clear
        });
      },

      // Resets all form data. Is called when pushing the clear button is pressed and after each update call 
      clear: function () {
        this.$refs.form.reset()
        this.username = '',
        this.password = '',
        this.passwordRe = '',
        this.email = '',
        this.data = ''
        this.imageName = ''
      }
    }
  }
</script>
