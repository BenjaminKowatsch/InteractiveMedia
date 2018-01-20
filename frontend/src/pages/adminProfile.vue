<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <span class="text-md-center">  
        <img v-if="adminUserData.imageUrl != null" src={{adminUserData.imageUrl}} alt="No picture found">
        <img v-else src="../assets/dummyProfilPicture.png"  alt="dummy picture">        
        <h1>Hello {{adminUserData.username}}</h1>
        <br>
        <p>sometext</p>
 				<v-flex xs12 class="text-xs-center text-sm-center text-md-center text-lg-center">
					<img :src="imageUrl" height="150" v-if="imageUrl"/>
					<v-text-field label="Select Image" @click='pickFile' v-model='imageName' prepend-icon='attach_file'></v-text-field>
					<input
						type="file"
						style="display: none"
						ref="image"
						accept="image/*"
						@change="onFilePicked"
					>
				</v-flex>
        <!-- <upload-button title="Browse" :selectedCallback="fileSelectedFunc">
        </upload-button> -->
        <v-btn :to="{path:'/overview'}">Back</v-btn>
      </span>
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
        //imageFile: '',
        isFile: false,
        imageFileUrl: '',
        title: "Image Upload",
        dialog: false,
		imageName: '',
		imageUrl: '',
		imageFile: ''
    
    
       
    }
    },

    mounted: function(){

      this.getOwnUser()        
    },

    methods: {



      
pickFile () {
            this.$refs.image.click ()
        },
		
		onFilePicked (e) {
			const files = e.target.files
			if(files[0] !== undefined) {
				this.imageName = files[0].name
				if(this.imageName.lastIndexOf('.') <= 0) {
					return
				}
				const fr = new FileReader ()
				fr.readAsDataURL(files[0])
				fr.addEventListener('load', () => {

          let data = new FormData();   
           let file = files[0];
           data.append('uploadField', file, file.name);
       
					this.imageUrl = fr.result
         // const file = files[0]
         // this.imageFile = files[0]
         // console.log(this.imageFile)
          this.uploadPicture(data) // this is an image file that can be sent to server...
				})
			} else {
				this.imageName = ''
				this.imageFile = ''
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
            console.log("Profilpicture: " + this.adminUserData.path)
        })
        .catch(e => {              
              console.log("Errors own user request (UserTableVuetify): " + e);           
            });
        },
      

     uploadPicture: function (data){
    
      console.log(data)
      var upload = {
          uploadField : data
      }
        
      console.log(upload)

       axios
          .post(Config.webServiceURL + "/v1/object-store/upload", data, {
          headers: { Authorization: "0 " + this.authToken, 'content-type': 'multipart/form-data' }
        })
        .then(function(response){
          console.log("Uploaded Picture")
          console.log(JSON.stringify(response))
          console.log(JSON.stringify(response.data.payload.path))
          this.imageFileUrl = response.data.payload.path
        })
        .catch(e => {
          console.log("Errors in POST Upload: " + e);
          console.log(e)
        });       
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
