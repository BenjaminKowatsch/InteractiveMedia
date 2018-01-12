<template>
  <!-- App -->
  <div id="app">

    <!-- Statusbar -->
    <!-- Main Views -->
    <!-- <f7-views>
      <f7-view id="main-view" navbar-through :dynamic-navbar="true" main> -->
        <!-- iOS Theme Navbar -->
        <!-- <f7-navbar v-if="$theme.ios">
          <f7-nav-center sliding>Debts² admin panel</f7-nav-center>
        </f7-navbar> -->
        <!-- Pages -->
       <!--  <f7-pages> -->
             <router-view/>

<!--           <login ref="loginForm"></login>
 -->
     <!--    </f7-pages>
      </f7-view>
    </f7-views> -->

   <!--  <f7-panel id="right_panel" right cover layout="dark" >
    <f7-view navbar-fixed name="right">
    <f7-pages>
      <f7-page>
        <f7-navbar title="Menü"/>
        <f7-list> -->
          <!-- TODO Neu eintragen machen-->
<!--           <f7-list-item @click="reload()" title="Neu eintragen" link-view="#main-view" link-close-panel></f7-list-item>
 --><!--  Just an Example for later implemenation:         
<f7-list-item link="/statistics" title="Statisiken" link-view="#main-view" link-close-panel></f7-list-item> !-->

         <!--  <f7-block inner>
            <f7-link @click="logout" close-panel id="logoutButton">Logout</f7-link>
          </f7-block>
        </f7-list>
      </f7-page>
    </f7-pages>
  </f7-view>
</f7-panel> -->
  </div>
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
   methods: {
    // reload: function () {
    //   location.reload();
/*     }
 */    /**
     * This function logs out the user by sending a POST request to the backend,
     * if the cookie containing the access token and the authentication type exists
     * Also depending on the authentication type the user will be logged out of google or facebook.
     * Afterwards a redirect to the login page will happen
     */
    /* logout: function() {
      this.checkAccessToken(accessToken =>{
        // Delete cookie for users who registered at Launometer
        Cookie.deleteCookie('accessToken'); */
        // Additionally sign out facebook and google users, if the AUTH_TYPE is set to facebook or google
/*         if(1 === accessToken.authType){
          // Sign out from google and delete cookie for users who signed in with google
          var auth2 = gapi.auth2.getAuthInstance();
          auth2.signOut().then(function() {
            console.log('Signed out google user');
          });
        } */
        // Post data to the backend to successfully logout the user
/*         axios.post(Config.webServiceURL+`/logout`, accessToken)
        .then(response => {
          console.log(JSON.stringify(response.data));
        })
        .catch(e => {
          console.log(JSON.stringify(e));
        });
      });
    } */
  }

}




</script>
