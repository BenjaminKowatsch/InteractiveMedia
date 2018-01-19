<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <span class="text-md-center">  
        <img v-if="adminUserData.imageUrl != null" src={{adminUserData.imageUrl}} alt="No picture found">
        <img v-else src="../assets/dummyProfilPicture.png"  alt="dummy picture">        
        <h1>Hello {{adminUserData.username}}</h1>
        <br>
        <p>sometext</p>
 <div class="file-btn">
    <input
      ref="input"
      type="file"
      :accept="accept"
      :disabled="disabled"
      @change="changed">
    <v-btn
      :disabled="disabled"
      @click="clicked"><slot>Browse</slot></v-btn>
</div>
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
    components: {
      
    },
   name: 'VFileBtn',
  props: {
    value: {
      type: File,
      default: null
    },
    accept: {
      type: String,
      default: 'image/*'
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },

    data () {
      return {
        authToken: "",
        adminUserData: [],
        imageFile: '',
        isFile: false,
        imageFileUrl: ''
    
       
    }
    },

    mounted: function(){

      this.getOwnUser()        
    },

    methods: {



      
    clicked: function() {
      this.$refs.input.click()
    },
    changed: function(e) {
      const file = e.target.files[0]
      if (file) {
        this.$emit('input', file)
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
            console.log("Profilpicture: " + this.adminUserData.imageUrl)
        })
        .catch(e => {              
              console.log("Errors own user request (UserTableVuetify): " + e);           
            });
        },
      }

   /*  uploadPicture(){

      var upload = {
        "uploadField": this.imageFile
      }

      axios
          .post(Config.webServiceURL + "/v1/object-store/upload", upload, {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(function(response){
          console.log("Uploaded Picture")
          console.log(JSON.stringify(response))
          console.log(JSON.stringify(response.data.payload.path))
          this.imageFileUrl = response.data.payload.path
        })
        .catch(e => {
          console.log("Errors in POST TRANSACTION: " + e);
          console.log(e)
        });       
    } */
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
