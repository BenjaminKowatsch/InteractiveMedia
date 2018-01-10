<template>
<vuetable ref="vuetable"
    :api-url="apiUrl"
      :fields="fields"
      :http-options="options"
      :data-path="payload.data">
    </vuetable>
</template>

<script>

import Vuetable from 'vuetable-2';
import Mixins from "../mixins.js";
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

export default ({
    components: {
    Vuetable    
    },
        props: ['authToken', 'url'],
 
   data() {
        return {
           fields: [
        'username', 'email'],
        apiUrl: this.url,
        options: {
            headers: {
          Authorization: "0 " + this.authToken 
        }
        }
    } 
        },
        mounted: function(){
            console.log("Users in Table")
            console.log(JSON.stringify(this.users))
            console.log("APIURL: " + this.apiUrl)
            console.log("AuthToken in Tables: " + this.authToken)
        },
        methods: {

      onFilterSet (filterText) {
      this.appendParams.filter = filterText
      Vue.nextTick( () => this.$refs.vuetable.refresh() )
    },
      onFilterReset () {
      delete this.appendParams.filter
      Vue.nextTick( () => this.$refs.vuetable.refresh() )
    }
        }
        
    
 

  
})
</script>