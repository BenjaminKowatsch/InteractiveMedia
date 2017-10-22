// Import Vue
import Vue from 'vue'

// Import F7
import Framework7 from 'framework7'

import VueTouch from 'vue-touch'

import VueCharts from 'vue-chartjs'

// Import F7 Vue Plugin
import Framework7Vue from 'framework7-vue'

// Import F7 iOS Theme Styles
import Framework7Theme from 'framework7/dist/css/framework7.ios.min.css'
import Framework7ThemeColors from 'framework7/dist/css/framework7.ios.colors.min.css'
/* OR for Material Theme:
import Framework7Theme from 'framework7/dist/css/framework7.material.min.css'
import Framework7ThemeColors from 'framework7/dist/css/framework7.material.colors.min.css'
*/

// Import App Custom Styles
import AppStyles from './css/app.css'

// Import Routes
import Routes from './routes.js'

// Import App Component
import App from './app'

// Import Cookie JS
import Cookie from './js/Cookie.js'

// Import Config
import Config from './js/Config.js'

import Mixins from './mixins.js'

Vue.use(VueTouch, {name: 'v-touch'})

// Init F7 Vue Plugin
Vue.use(Framework7Vue)

Vue.use(VueCharts)


var startApp = function(onLoadingFinished){

  // Load Google API asynchronously
  window.gapi.load('auth2', () => {
    // Init Google API with
    const auth2 = window.gapi.auth2.init(Config.googleParams);
    console.log("Google API initialized");

    FB.init({
      appId: Config.facebookParams.appId,
      cookie: true, // enable cookies to allow the server to access the session
      xfbml: true, // parse social plugins on this page
      version: Config.facebookParams.version // use graph api version
    });
    console.log("Facebook API initialized");
    onLoadingFinished();
  });
};

var onLoadingFinished = function(){

  // Init App
  var vm = new Vue({
    el: '#app',
    mixins: [ Mixins ],
    template: '<app ref="app" ><app/>',
    // Init Framework7 by passing parameters here
    framework7: {
      root: '#app',
      modalTitle: 'Launometer',
      /* Uncomment to enable Material theme: */
      material: false,
      /* Enable browser hash navigation */
      pushState: true,
      /* Set oparator for browser hash navigation */
      pushStateSeparator: '#',
      pushStateOnLoad: false,
      //animatePages : false,
      routes: Routes,
    },
    // Register App Component
    components: {
      app: App
    },
    data: function(){
      return{
        loginStatus: false
      };
    },
    created: function(){
      // Only update the loginStatus if the user is not already logged in with facebook
      this.updateLoginStatus();
    },
    mounted: function(){
      // Redirect to '/overview' if user is still logged in
      console.log("Login Status: "+ this.loginStatus);
      if(true === this.loginStatus){
        this.redirect("/overview", false, false, false);
      }
      this.addPushStatePanel();
    },
    methods:{
      /**
       * Function adds an event listener to framework7 so if on any page the back button will be clicked the side panel will be closed
       */
       addPushStatePanel: function(){
         var framework7 = this.$f7;
         framework7.onPageBack("*",function(page){
           framework7.closePanel();
         });
       },
       updateLoginStatus: function() {
         // Check if cookies exist
         // If a cookie exists, set the loginStatus to true
         // The function checkAccessToken cannot be used here because at 'created' the framework7 instance is not defined ('this.$f7')
         // and due to the redirect to the login page, you would run into an infinite loop
         var accessToken = Cookie.getJSONCookie('accessToken')
         if (accessToken !== '') {
           var authOptions = ["Launometer","Google","Facebook"];
           console.log(authOptions[accessToken.authType]+" Cookie exists. Redirecting ...");
           this.loginStatus = true;
         };
       }
    }
  });
   // Delete all entries after logout
  document.getElementById("logoutButton").onclick = function () {
  	vm.$children[0].$refs.loginForm.reset();
  };

};

startApp(onLoadingFinished);
