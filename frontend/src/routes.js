import Router from 'vue-router'
import login from './pages/login.vue'
import overview from './pages/overview.vue'

export default {
    routes: [{
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

}