// Import Vue
import Vue from 'vue'

import VueRouter from 'vue-router'

// Import F7

import VueTouch from 'vue-touch'

import VueCharts from 'vue-chartjs'

// Import F7 Vue Plugin

// Import App Custom Styles
import AppStyles from './css/app.css'

// Import Routes
import Routes from './routes.js'

// Import App Component
import App from './app'

import login from './pages/login.vue'
import overview from './pages/overview.vue'

// Import Cookie JS
import Cookie from './js/Cookie.js'

// Import Config
import Config from './js/Config.js'

import Mixins from './mixins.js'
import { TableComponent, TableColumn } from 'vue-table-component';

Vue.component('table-component', TableComponent);
Vue.component('table-column', TableColumn);
Vue.use(VueTouch, { name: 'v-touch' });
Vue.use(VueRouter)


// Init F7 Vue Plugin

Vue.use(VueCharts)

var startApp = function(onLoadingFinished) {
    onLoadingFinished();
}

var onLoadingFinished = function() {


    //ToDO: Try to do the routes into routes.js
    const routes = [{
            path: '/',
            name: "login",
            component: login
        },
        {
            path: '/overview/',
            name: "overview",
            component: overview
        },
    ]


    const router = new VueRouter({
        routes
    })

    // Init App
    var vm = new Vue({
        el: '#app',
        mixins: [Mixins],
        template: '<app ref="app" ><app/>',
        router,
        // Init Framework7 by passing parameters here
        /*         framework7: {
                    root: '#app',
                    modalTitle: 'Debts² Admin Panel',
                    //Uncomment to enable Material theme: 
                    material: false,
                   //  Enable browser hash navigation 
                    pushState: true,
                   // Set oparator for browser hash navigation 
                    pushStateSeparator: '#',
                    pushStateOnLoad: false,
                    //animatePages : false,
                    routes: Routes,
                }, */
        // Register App Component
        components: {
            app: App
        },
        data: function() {
            return {
                loginStatus: false
            };
        },
        created: function() {
            // Only update the loginStatus if the user is not already logged in with facebook
            this.updateLoginStatus();
        },
        mounted: function() {
            // Redirect to '/overview' if user is still logged in
            console.log("Login Status: " + this.loginStatus);
            if (true === this.loginStatus) {
                this.redirect("/overview", false, false, false);
            }
            /*             this.addPushStatePanel();
             */
        },
        methods: {
            /**
             * Function adds an event listener to framework7 so if on any page the back button will be clicked the side panel will be closed
             */
            /*   addPushStatePanel: function() {
                  var framework7 = this.$f7;
                  framework7.onPageBack("*", function(page) {
                      framework7.closePanel();
                  });
              }, */
            updateLoginStatus: function() {
                // Check if cookies exist
                // If a cookie exists, set the loginStatus to true
                // The function checkAccessToken cannot be used here because at 'created' the framework7 instance is not defined ('this.$f7')
                // and due to the redirect to the login page, you would run into an infinite loop
                var accessToken = Cookie.getJSONCookie('accessToken')
                if (accessToken !== '') {
                    //var authOptions = ["Launometer","Google","Facebook"];
                    //Hint: if auth doesnt work after removing mocking, maybe authOptions has to be modified again
                    var authOptions = "Debts² Admin Panel";

                    console.log(authOptions[accessToken.authType] + " Cookie exists. Redirecting ...");
                    this.loginStatus = true;
                };
            }
        }
    });
    // Delete all entries after logout
    /*     document.getElementById("logoutButton").onclick = function() {
            vm.$children[0].$refs.loginForm.reset();
        };
     */
};

startApp(onLoadingFinished);