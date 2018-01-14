
<template>
   <div class="groupTable">
       <table-component
       :data="fetchData">
           <table-column show="name" label="Groupname"></table-column>
           <table-column show="groupId" label="Group ID"></table-column>
           <table-column show="createdAt" label="Date of creation"></table-column>
           <table-column show="countUsers" label="Users"></table-column>
           <table-column show="countTransactions" label="Transactions"></table-column>
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

    props: ['groups'],

    data() {
        return {
            fetchData: this.groups
        }
    },
})
</script>