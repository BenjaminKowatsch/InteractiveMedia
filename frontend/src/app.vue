<template>

   <v-app id="inspire" :dark="isDark">
        <v-navigation-drawer
      clipped
      absolute
      v-model="drawer"
      disable-resize-watcher="true"
      app>

       <v-list dense>
        <v-list-tile @click="changeTheme">
          <v-list-tile-action>
            <v-icon>invert_colors</v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title>Change Theme</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
        <v-divider inset></v-divider>
      </v-list>
    </v-navigation-drawer> 
    <v-toolbar app fixed clipped-left>
      <v-toolbar-side-icon @click="drawer = !drawer"></v-toolbar-side-icon>
      <v-spacer></v-spacer>
      <v-toolbar-title>Debts² admin panel</v-toolbar-title>
      <v-spacer></v-spacer>
      <v-btn icon @click="logout">
        <v-icon dark>fa-sign-out</v-icon>
      </v-btn>
      
    </v-toolbar> 
     <v-snackbar
          :timeout="3000"
          :bottom="true"
          :multi-line="true"
          class="red"
          v-model="notLoggedInAlert"
        >
          Not logged in
          <v-btn dark flat @click.native="notLoggedInAlert = false">X</v-btn>
        </v-snackbar>
    <v-content>
          <router-view></router-view>
    </v-content>
    <v-footer app fixed>
      <span>&copy; 2017 Debts² Dev Team </span>
    </v-footer>
  </v-app>
</template>

<script>
import Mixins from './mixins.js'
import Login from './pages/login'
import axios from 'axios'
import Cookie from './js/Cookie.js'
import Config from './js/Config.js'

export default {
  name: 'app',
  mixins: [ Mixins ],
  components: {
    Login
  },
  data: () => ({
      isDark: true,
      drawer: false,
      notLoggedInAlert: false
    }),
    props: {
      source: String
    },

   methods: {

     changeTheme: function() {

      if(this.isDark == true)
      {
        this.isDark = false
        console.log(this.isDark)
      }
      else if(this.isDark == false)
      {
        this.isDark = true
        console.log(this.isDark)
      }
     },

     toogleDrawer: function(){

       if(this.drawer == true)
       {
         this.drawer = false
       }
       else if(this.drawer == false)
       {
         this.drawer = true
       }
     },
/*     reload: function () {
       location.reload();
     } */
 
     logout: function() {

      var authToken = Cookie.getJSONCookie('accessToken').accessToken
      //Check for existing accessToken
      if(authToken){

        console.log("Cookie exists :")
        console.log(authToken)

        //Post data to the backend to successfully logout the user and redirect to login page
        axios
          .post(Config.webServiceURL + `/v1/users/logout`, authToken, {
            headers: {
              Authorization: "0 " + authToken
            }
          })
          .then(response => {
            console.log("Logout response:")
            console.log(JSON.stringify(response.data));
            Cookie.deleteCookie("accessToken");
            this.redirect("/", false, false, true);
            console.log("Logging out...")
          })
          .catch(e => {
            console.log(JSON.stringify(e));
          });
    }
    else {
      this.notLoggedInAlert = true; 
    }
  }
   }
}

</script>
