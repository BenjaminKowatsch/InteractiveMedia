// Import Vue
import Vue from 'vue'

// Import VueRouter (routing)
import VueRouter from 'vue-router'

// Import Vuetify (material component framework)
import Vuetify from 'vuetify'

// Import VueCharts (charts)
import VueCharts from 'vue-chartjs'

// Import App Custom Styles
import AppStyles from './css/app.css'

// Import App Component
import App from './app'

import login from './pages/login.vue'
import overview from './pages/overview.vue'
import adminProfile from './pages/adminProfile.vue'
import about from './pages/about.vue'
import notFound from './pages/notFound.vue'
import noPermission from './pages/noPermission.vue'

// Import Config
import Config from './js/Config.js'

// Import Cookie
import Cookie from "./js/Cookie.js";

// Import Mixins
import Mixins from './mixins.js'

// Using directives
Vue.use(VueRouter)
Vue.use(Vuetify)
Vue.use(VueCharts)


var startApp = function(onLoadingFinished) {
    onLoadingFinished();
}

var onLoadingFinished = function() {

    const router = new VueRouter({

        linkActiveClass: 'active',
        transitionOnLoad: true,
        routes: [
            { path: "/", name: "login", component: login },
            { path: '/overview', name: "overview", component: overview },
            { path: "/admin", name: "admin", component: adminProfile },
            { path: "/about", name: "about", component: about },
            { path: "/noPermission", name: "noPermission", component: noPermission },
            { path: '*', component: notFound },
            { path: '/*/', component: notFound },
        ]
    })

    // Init App
    var vm = new Vue({
        el: '#app',
        mixins: [Mixins],
        template: '<app ref="app" ><app/>',
        router,
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

            this.updateLoginStatus();
        },
        mounted: function() {
            // Redirect to '/overview' if user is still logged in
            console.log("Login Status: " + this.loginStatus);
            if (true === this.loginStatus) {
                this.redirect("/overview");
            }

        },
        methods: {

            updateLoginStatus: function() {
                // Check if cookies exist
                // If a cookie exists, set the loginStatus to true
                var accessToken = Cookie.getJSONCookie("accessToken").accessToken;
                if (accessToken !== '' && accessToken !== undefined && accessToken !== 'undefined') {
                    this.loginStatus = true;
                };
            }
        }
    });
};

startApp(onLoadingFinished);