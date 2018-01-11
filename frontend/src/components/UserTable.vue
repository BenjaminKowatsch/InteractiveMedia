
<template>
   <div class="userTable">
       <table-component
       :data="fetchData">
           <table-column show="username" label="Username"></table-column>
           <table-column show="email" label="Email"></table-column>
           <table-column show="userId" label="User ID"></table-column>
           <table-column show="authType" label="Logintyp"></table-column>
           <table-column show="countGroupIds" label="Groups"></table-column>
       </table-component>
   </div>
</template>
     
<script>
import Mixins from "../mixins.js";
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

//ToDo: implement something to do paging
export default ({

    props: ['users'],

    data() {
        return {
            fetchData: this.users

        }
    },
})
</script>