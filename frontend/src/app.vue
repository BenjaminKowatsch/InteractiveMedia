<!-- TEMPLATE for main view
  * Description: Entrypoint for the app. Contains an app wide toolbar and sidepanel (navigation-drawer)
 -->
<template>
  <main>
   <v-app id="inspire" :dark="isDark" v-if="isDark">
      {{isDark}}
      <v-navigation-drawer
      clipped
      fixed
      v-model="drawer"
      disable-resize-watcher="true"
      disable-route-watcher="true"
      app>
        <v-list>
          <v-list-tile @click="checkForDashboard">
            <v-list-tile-action>
              <v-icon>fa-th</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>Dashboard</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile @click="checkForAdminProfile">
            <v-list-tile-action>
              <v-icon>fa-user-o </v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>Admin profile</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile :to="{path:'/about'}">
            <v-list-tile-action>
              <v-icon>fa-info-circle</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>About</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile>
            <v-divider></v-divider>
          </v-list-tile> 
          <v-list-tile>
            <v-switch
              label="Darktheme"
              v-model="isDark"
            ></v-switch>  
          </v-list-tile>       
        </v-list>
      </v-navigation-drawer> 
      <v-toolbar app fixed clipped-left>
        <v-toolbar-side-icon @click="drawer = !drawer"></v-toolbar-side-icon>
        <v-spacer></v-spacer>
        <v-toolbar-title>Debts² admin panel</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-tooltip bottom>
          <v-btn icon slot="activator" @click="logout">
            <v-icon>fa-sign-out</v-icon>
          </v-btn>
          <span>Logout</span>
         </v-tooltip>
      </v-toolbar> 
      <v-snackbar
        :timeout="3000"
        :bottom="true"
        class="red darken-4"
        v-model="notLoggedInAlert"
      >
         Not logged in
        <v-btn dark flat @click.native="notLoggedInAlert = false">X</v-btn>
      </v-snackbar>
      <v-snackbar
        :timeout="3000"
        :bottom="true"
        class="green darken-4"
        v-model="loggedOut"
      >
        Sucessfully logged out
        <v-btn dark flat @click.native="notLoggedInAlert = false">X</v-btn>
      </v-snackbar>
      <v-content>
        <router-view></router-view>
      </v-content>
      <v-footer app fixed>
        <span>&copy; 2017 Debts² Dev Team </span>
      </v-footer>
    </v-app>
    <v-app id="inspire" :dark="isDark" v-if="!isDark">
      <v-navigation-drawer
      clipped
      fixed
      v-model="drawer"
      disable-resize-watcher="true"
      disable-route-watcher="true"
      class="green lighten-2"    
      app>
        <v-list>
          <v-list-tile @click="checkForDashboard">
            <v-list-tile-action>
              <v-icon>fa-th</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>Dashboard</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile @click="checkForAdminProfile">
            <v-list-tile-action>
              <v-icon>fa-user-o </v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>Admin profile</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile :to="{path:'/about'}">
            <v-list-tile-action>
              <v-icon>fa-info-circle</v-icon>
            </v-list-tile-action>
            <v-list-tile-content>
              <v-list-tile-title>About</v-list-tile-title>
            </v-list-tile-content>
          </v-list-tile>
          <v-list-tile>
            <v-divider></v-divider>
          </v-list-tile> 
          <v-list-tile>
            <v-switch
              label="Darktheme"
              v-model="isDark"
            ></v-switch>  
          </v-list-tile>       
        </v-list>
      </v-navigation-drawer> 
      <v-toolbar app fixed clipped-left class="green lighten-1">
        <v-toolbar-side-icon @click="drawer = !drawer"></v-toolbar-side-icon>
        <v-spacer></v-spacer>
        <v-toolbar-title>Debts² admin panel</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-tooltip bottom>
          <v-btn icon slot="activator" @click="logout">
            <v-icon>fa-sign-out</v-icon>
          </v-btn>
          <span>Logout</span>
         </v-tooltip>
      </v-toolbar> 
      <v-snackbar
        :timeout="3000"
        :bottom="true"
        class="red darken-4"
        v-model="notLoggedInAlert"
      >
         Not logged in
        <v-btn dark flat @click.native="notLoggedInAlert = false">X</v-btn>
      </v-snackbar>
      <v-snackbar
        :timeout="3000"
        :bottom="true"
        class="green darken-4"
        v-model="loggedOut"
      >
        Successfully logged out
        <v-btn dark flat @click.native="notLoggedInAlert = false">X</v-btn>
      </v-snackbar>
      <v-content class="green lighten-3">
        <router-view></router-view>
      </v-content>
      <v-footer app fixed class="green lighten-1">
        <span>&copy; 2017 Debts² Dev Team </span>
      </v-footer>
    </v-app>
  </main>
</template>

<script>
import Mixins from './mixins.js'
import axios from 'axios'
import Cookie from './js/Cookie.js'
import Config from './js/Config.js'

export default {
  name: 'app',
  mixins: [ Mixins ],
  components: {
  },
  data: () => ({
      isDark: true,
      drawer: false,
      notLoggedInAlert: false,
      loggedOut: false,
    }),
    props: {
      source: String
    },

   methods: {

     // Checks if darkTheme is enabled or not
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

    // Toogles the Sidepanel
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

     // When navigating to the dashboard over sidepanel, checks if the user is logged in (a cookie exists)
     // If yes, he will be redirected to the overview page, if not he will be redirected to the noPermission site
     checkForDashboard: function(){

        var authToken = Cookie.getJSONCookie('accessToken').accessToken

        if(authToken)
        {
          this.redirect("/overview")
        }
        else
        {
          this.redirect("/noPermission")
        }
    },
     
     // When navigating to the adminProfile over sidepanel, checks if the user is logged in (a cookie exists)
     // If yes, he will be redirected to the adminPanel page, if not he will be redirected to the noPermission site
     checkForAdminProfile: function(){

      var authToken = Cookie.getJSONCookie('accessToken').accessToken

      if(authToken)
      {
        this.redirect("/admin")
      }
      else
      {
        this.redirect("/noPermission")
      }
     },

     // Logs out the current user and delets the accessToken
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
            this.loggedOut = true
          })
          .catch(e => {
            console.log("Error while logging out:")
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
