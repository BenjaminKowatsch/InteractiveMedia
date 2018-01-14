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
    <v-toolbar app fixed clipped-left prominent="true">
 <v-toolbar-side-icon @click="drawer = !drawer"></v-toolbar-side-icon>      <v-spacer></v-spacer>
        <v-toolbar-title>Debts² admin panel</v-toolbar-title>
      <v-spacer></v-spacer>
    </v-toolbar>
    <v-content>
      <v-container fluid fill-height>
        <v-layout justify-center align-center>
<!--           <v-tooltip right>
            <v-btn icon large :href="source" target="_blank" slot="activator">
              <v-icon large>code</v-icon>
            </v-btn>
            <span>Source</span>
          </v-tooltip> -->
          <router-view></router-view>
          </v-layout>
      </v-container>
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
      drawer: false
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
     }
/*     reload: function () {
       location.reload();
     } */
 
  }

}

</script>
