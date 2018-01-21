<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <span class="text-md-center">  
        <img v-if="hasImageUrl" v-bind:src="dummy" alt="No picture found">
        <img v-else src="../assets/logo.png"  alt="Debts² logo" class="logo">
        <h1 v-if="hasData">Welcome back, {{adminUserData.username}}!</h1>
        <h3>Current email: {{adminUserData.email}}</h3>
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
					>
          <v-btn
            @click="checkUpdate"
            :disabled="!valid"
          >
            Update
          </v-btn>
          <v-btn @click="clear">Clear</v-btn>
        </v-form>
        <v-btn :to="{path:'/overview'}">Back</v-btn>
        
        <span v-if="hasImageUrl">{{adminUserData.imageUrl}}</span> 
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
        hasImageUrl: false,
        //imageDataUrl: '',
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

      this.getOwnUser()  

    },

    methods: {



      
    pickFile () {

            this.$refs.image.click ()
        },
		
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
       
					//this.imageUrl = fr.result
          //this.uploadPicture(data) // this is an image file that can be sent to server...
				})
			} else {
				this.imageName = ''
				this.imageUrl = ''
			}
		},
    

      getOwnUser: function(){

        this.authToken = Cookie.getJSONCookie("accessToken").accessToken;

        axios
        .get(Config.webServiceURL + "/v1/users/user", {
           headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {
            this.adminUserData = response.data.payload 
            console.log(JSON.stringify(this.adminUserData))
            this.hasData = true
            if(this.adminUserData.imageUrl != null){

              this.downloadImage()
            }
        })
        .catch(e => {              
              console.log("Errors own user request (UserTableVuetify): " + e);           
            });
        },
      

     uploadImage: function (data){
    
      console.log(data)
      var upload = {
          uploadField : data
      }
        
      console.log(upload)

       axios
          .post(Config.webServiceURL + "/v1/object-store/upload", data, {
          headers: { Authorization: "0 " + this.authToken, 'content-type': 'multipart/form-data' }
        })
        .then(response => {
          console.log("Uploaded Picture"),
          console.log(JSON.stringify(response)),
          console.log(JSON.stringify(response.data.payload.path)),
          this.imageFileUrl = response.data.payload.path,
          this.updateImageUrl(this.imageFileUrl)          
        })
        .catch(e => {
          console.log("Errors in POST Upload: " + e);
          console.log(e)
        });       
     },
     
  downloadImage: function(){

    var downloadUrl = this.adminUserData.imageUrl
    console.log(downloadUrl)

    axios
        .get(downloadUrl, {  
           headers: { Authorization: "0 " + this.authToken}}
        )
        .then(response =>  {
const fs = require('fs');

            response.data.pipe(fs.createWriteStream('image.jpg'))
         
        console.log(response)
/*         var file = new File(response.data)
        //file = response.data
        const fr = new FileReader ()
				fr.readAsDataURL(file)
				fr.addEventListener('load', () => {    
        this.URL = fr.result
        console.log("URL: ")
        console.log(URL)
            
        console.log(this.hasImageUrl)
        })  */
  })

/*             var reader = new window.FileReader();
            reader.readAsDataURL(response.data); 

            reader.onload = function() {

            this.imageDataUrl = reader.result;
            console.log("ImageDataUrl")
            console.log(this.imageDataUrl)
            imageElement.setAttribute("src", this.imageDataUrl); 
            }  */
        
        .catch(e => {              
              console.log("Errors download image " + e);           
            });
    },
  
   updateImageUrl: function(imageFileUrl){

     var createdImageUrl = Config.webServiceURL + "/v1/object-store/download?filename=" + imageFileUrl
     console.log(createdImageUrl)

     var userUpdateImage = {
       imageUrl: createdImageUrl
     }
     console.log(userUpdateImage)
    axios.put(Config.webServiceURL + "/v1/users/user", userUpdateImage, {
                headers: { Authorization: "0 " + this.authToken }
              })
              .then(response => 
                  console.log(JSON.stringify(response)), 
                  this.clear(),
                  this.successUpdate = true
              )
              .catch(e => {
                console.log("Errors update imageUrl: " + e);
              });
   },

   checkUpdate: function () {

        if (this.$refs.form.validate()) {

          if(this.imageName.length > 0){
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

              var updateField = {
                username: this.username,
                password: this.password
              }
              this.update(updateField)
            } 
            else
            {
            this.passwordMatching = true
            }
          }
          if(this.username.length > 0 && this.password.length > 0 && this.email.length > 0)
          {
            if(this.password == this.passwordRe){

              var updateField = {
                username: this.username,
                password: this.password,
                email: this.email
              }
              this.update(updateField)
          }
            else
            {
            this.passwordMatching = true
            }
          }


          if(this.username.length > 0 && this.password.length == 0 && this.email.length > 0)
          {
              var updateField = {
                username: this.username,
                email: this.email
              }
              this.update(updateField)
          }

          if(this.username.length == 0 && this.password.length > 0 && this.email.length > 0)
          {
            if(this.password == this.passwordRe)
            {
              var updateField = {
                password: this.password,
                email: this.email
              }
              this.update(updateField)
            }
            else
            {
              this.passwordMatching = true
            }
          }

          if(this.username.length == 0 && this.password.length > 0 && this.email.length == 0)
          {
            if(this.password == this.passwordRe)
            {
              var updateField = {
                password: this.password
              }
              this.update(updateField)
            }
            else
            {
              this.passwordMatching = true
            }
          }

          if(this.username.length == 0 && this.password.length == 0 && this.email.length > 0)
          {
              var updateField = {
                email: this.email,
              }
              this.update(updateField)            
          }
        }      
   },

   update: function(field){

            axios.put(Config.webServiceURL + "/v1/users/user", field, {
                headers: { Authorization: "0 " + this.authToken }
              })
              .then(response => {
                  console.log("Update response")
                  console.log(JSON.stringify(response))
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

<style scoped>
  .file-btn {
    display: inline-block;
  }
  .file-btn input[type=file] {
    position: absolute;
    filter: alpha(opacity=0);
    opacity: 0;
  }
</style>
