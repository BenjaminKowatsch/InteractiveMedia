<!--
 -->
<template>
 <v-layout row>
    <v-flex xs12 sm6 offset-sm3>
        <v-text-field
            label="Search for group"
            prepend-icon="search"
            v-model="groupId"
            @keyup.enter="getGroup()"  
          ></v-text-field>
          <v-menu v-if="hasUsers">
            <v-btn slot="activator">Users</v-btn>
            <v-list>
              <v-list-tile v-for="user in userList" :key="user.username" @click="">
                <v-list-tile-title @click="selectUser(user), isUser=true">{{ user.username }}</v-list-tile-title>
              </v-list-tile>
            </v-list>
          </v-menu>
          <v-menu v-if="hasTransactions">
            <v-btn  dark slot="activator">Transactions</v-btn>
            <v-list>
              <v-list-tile v-for="transaction in transactionList" :key="transaction.amount" @click="">
                <v-list-tile-title @click="selectTransaction(transaction), isTransaction=true">{{ transaction.amount }}</v-list-tile-title>
              </v-list-tile>
            </v-list>
          </v-menu>
        <span v-if="isUser">{{selectedUser.username}}</span>
        <span v-if="isTransaction">{{selectedTransaction.amount}}</span>
    </v-flex>
 </v-layout>      
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
          isTransaction: false,
          text: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'
      }
    },

    mounted: function(){ 
      this.groupId = ""


    },

    methods: {

           getGroup: function(){

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

          }

          

    }
  }
</script>
