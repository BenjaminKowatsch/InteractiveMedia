<!--
 -->
<template>
<v-container fluid fill-height>
 <v-layout justify-center align-center> 
    <v-flex xs12 sm12 md6 lg6 xl6>
      <v-text-field
        label="Search for group"
        prepend-icon="search"
        v-model="groupId"
        @keyup.enter="getGroup()"
      ></v-text-field>
      <v-card v-if="hasUsers || hasTransactions" height="150px">
      <v-card-title >
        <v-menu  v-if="hasUsers" top min-width="150px">
          <v-btn slot="activator">Users</v-btn>
          <v-list>
            <v-list-tile v-for="user in userList" :key="user.username" @click="">
              <v-list-tile-title @click="selectUser(user), isUser=true">{{ user.username }}</v-list-tile-title>
            </v-list-tile>
          </v-list>
        </v-menu>
        <v-menu v-if="hasTransactions" top min-width="150px">
          <v-btn slot="activator">Transactions</v-btn>
          <v-list>
            <v-list-tile v-for="transaction in transactionList" :key="transaction.amount" @click="">
              <v-list-tile-title @click="selectTransaction(transaction), isTransaction=true">{{ transaction.amount }}</v-list-tile-title>
            </v-list-tile>
          </v-list>
        </v-menu>
        <v-btn v-if="hasTransactions || hasUsers" @click="reset()">Reset</v-btn>
      </v-card-title>
      <v-card-text v-if="isUser">{{selectedUser.username}}</v-card-text>
      <v-card-text v-if="isTransaction">{{selectedTransaction.amount}}</v-card-text>
    </v-flex>       
 </v-layout>
</v-container>      
</template>

<script>

import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

  export default {
    props: ['authToken', 'groups'],

    data () {
      return {
          groupId: '',
          selectedUser: [],
          selectedTransaction: [],
          userList: [],
          transactionList: [],
          hasUsers: false,
          hasTransactions: false,
          isUser: false,
          isTransaction: false
      }
    },

    mounted: function(){ 
      this.groupId = ""


    },

    methods: {

           getGroup: function(){
            
            this.hasUsers = false
            this.hasTransactions = false
            this.isUser = false
            this.isTransaction = false

            axios.get(Config.webServiceURL + "/v1/admin/groups/" + this.groupId, {
                headers: { Authorization: "0 " + this.authToken }
              })
              .then(response => {
                var group = []
                console.log(response)
                group = response.data.payload
                this.userList = group.users
                this.transactionList = group.transactions
                console.log(JSON.stringify(this.userList))
                console.log(JSON.stringify(this.transactionList))
                if(this.userList.length > 0){
                  this.hasUsers = true
                }
                if(this.transactionList.length > 0){
                  this.hasTransactions = true
                }
                console.log(this.userList.length)
                console.log(this.transactionList.length)

/*                 var users = group.users.length
                var transactions = group.transactions.length
                for(let i = 0; i < users; i++){
                  this.userList.push(group.users[i])
                }
                for(let i = 0; i < transactions; i++){
                  this.transactionList.push(group.transactions[i])
                }
                console.log(JSON.stringify(this.transactionList))
                console.log(this.transactionList.length )   */             
              })
              .catch(e => {
                console.log("Errors get group: " + e);

              });
          },

          selectUser: function(user){
            this.isTransaction = false
            this.selectedUser = user 
            console.log("Selected User:")
            console.log(this.selectedUser)

          },

           selectTransaction: function(transaction){
            this.isUser = false
            this.selectedTransaction = transaction 
            console.log("Selected Transaction:")
            console.log(this.selectedTransaction)

          },

          reset: function(){
              this.selectedUser = [],
              this.selectedTransaction = [],
              this.isUser = false,
              this.isTransaction = false
          }

          

    }
  }
</script>
