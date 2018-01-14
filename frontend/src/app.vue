<template>

   <v-app id="inspire" dark>
      <!-- <v-navigation-drawer
      clipped
      fixed
      v-model="drawer"
      app>

       <v-list dense>
        <v-list-tile @click="theme = dark">
          <v-list-tile-action>
            <v-icon>dashboard</v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title>Dashboard</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
        <v-list-tile @click="theme = light">
          <v-list-tile-action>
            <v-icon>settings</v-icon>
          </v-list-tile-action>
          <v-list-tile-content>
            <v-list-tile-title>Settings</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
    </v-navigation-drawer> --> 
    <v-toolbar app fixed clipped-left>
      <v-spacer></v-spacer>
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
      // theme: "dark",
      drawer: null
    }),
    props: {
      source: String
    },
   methods: {
/*     reload: function () {
       location.reload();
     } */
 
  }

}

</script>
