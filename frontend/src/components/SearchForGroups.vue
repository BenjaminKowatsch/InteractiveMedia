<!-- TEMPLATE for Group search
  * Description: Search for a group and chose specific users or transactions of the group to be displayed in detail
 -->
<template>
  <v-container fluid fill-height>
    <v-layout  justify-center align-center> 
      <v-flex xs12 sm12 md12 lg12 xl12>
        <v-text-field
          label="Search for group"
          prepend-icon="search"
          v-model="groupId"
          @keyup.enter="getGroup()"
        ></v-text-field>
        <v-card v-if="hasUsers || hasTransactions">
        <v-card-title >
          <v-layout justify-center align-center>
          <v-menu  v-if="hasUsers" top>
            <v-btn slot="activator">Users</v-btn>
            <v-list>
              <v-list-tile v-for="user in userList" :key="user.username" @click="">
                <v-list-tile-title @click="selectUser(user), isUser=true">{{ user.username }}</v-list-tile-title>
              </v-list-tile>
            </v-list>
          </v-menu>
          <v-menu v-if="hasTransactions" top>
            <v-btn slot="activator">Transactions</v-btn>
            <v-list>
              <v-list-tile v-for="transaction in transactionList" :key="transaction.amount" @click="">
                <v-list-tile-title @click="selectTransaction(transaction), isTransaction=true">{{ transaction.amount }}</v-list-tile-title>
              </v-list-tile>
            </v-list>
          </v-menu>
          <v-btn v-if="hasTransactions || hasUsers" @click="reset()">Reset</v-btn>
          </v-layout>
          <v-card-text v-if="isUser">UserID: {{selectedUser.userId}}, Name: {{selectedUser.username}} </v-card-text>
          <v-card-text v-if="isUser">Email: {{selectedUser.email}}</v-card-text>
          <v-card-text v-if="isTransaction">Amount: {{selectedTransaction.amount}}, Info: {{selectedTransaction.infoName}}</v-card-text>
          <v-card-text v-if="isTransaction">Longitude: {{selectedTransaction.infoLocation.longitude}}, Latitude: {{selectedTransaction.infoLocation.latitude}}</v-card-text>
          <v-card-text v-if="isTransaction">Paid by: {{selectedTransaction.paidBy}},  Split: {{selectedTransaction.split}}</v-card-text>
          <v-card-text v-if="isTransaction">Created: {{selectedTransaction.infoCreatedAt}}, Published: {{selectedTransaction.publishedAt}}</v-card-text>
        </v-card-title>
        </v-card>
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

           // Request the group which id is entered in the search field
           // Then check for the group if there are transactions or users
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
                group = response.data.payload
                this.userList = group.users
                this.transactionList = group.transactions
                if(this.userList.length > 0){
                  this.hasUsers = true
                }
                if(this.transactionList.length > 0){
                  this.hasTransactions = true
                }
              })
              .catch(e => {
                console.log("Errors get group: " + e);
              });
          },

          // If a user was selected in the dropdown menue, save the users into selectedUser to be shown in output
          selectUser: function(user){
            this.isTransaction = false
            this.selectedUser = user 
          },
                     
           // If a transaction was selected in the dropdown menue, save the transaction into selectedTransaction to be shown in output
          selectTransaction: function(transaction){
            this.isUser = false
            this.selectedTransaction = transaction 
          },

          // Resets the output 
          reset: function(){
              this.selectedUser = [],
              this.selectedTransaction = [],
              this.isUser = false,
              this.isTransaction = false
          }
    }
  }
</script>
