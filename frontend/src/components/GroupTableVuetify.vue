<!-- TEMPLATE for group table
  * Description: Table to display all groups
 -->
<template>
  <v-container>
      <v-text-field
        append-icon="search"
        label="Search"
        hide-details
        v-model="search"
      ></v-text-field>
    <v-data-table
        v-bind:headers="headers"
        v-bind:items="items"
        v-bind:search="search"
        class="elevation-1"
      >
      <template slot="items" slot-scope="props">
        <td class="text-xs-right">{{ props.item.name }}</td>
        <td class="text-xs-right">{{ props.item.groupId }}</td>
        <td class="text-xs-right">{{ props.item.createdAt }}</td>
        <td class="text-xs-right">{{ props.item.imageUrl }}</td>
        <td class="text-xs-right">{{ props.item.countUsers }}</td>
        <td class="text-xs-right">{{ props.item.countTransactions }}</td>
      </template>
      <template slot="no-data">
      <v-alert :value="true" color="error" icon="warning">
        Sorry, nothing to display here :(
      </v-alert>
    </template>
     <template slot="footer">
        <td class="text-xs-left" colspan="100%">
          <strong>Total Groups: {{ items.length }}</strong>
        </td>
      </template>
      <template slot="pageText" slot-scope="{ pageStart, pageStop }">
        From {{ pageStart }} to {{ pageStop }}
      </template>
    </v-data-table>
  </v-container>
</template>

<script>
import axios from "axios";
import Config from "../js/Config.js";

  export default {

    props: ['groups', 'authToken'],

    data () {
      return {
        tmp: '',
        search: '',
        pagination: {},
        headers: [
          { text: 'Groupname', value: 'name' },
          { text: 'Group ID', value: 'groupId', align: 'center', sortable: false},
          { text: 'Date of creation', value: 'createdAt', align: 'center'},
          { text: 'Groupimage', value: 'imageUrl' },
          { text: 'Users', value: 'countUsers' },
          { text: 'Transactions', value: 'countTransactions' },
        ],
        items: this.groups,
        amounts: []
      }
    },

    mounted: function(){
      this.amounts = []
     // this.createTransaction()
      

        
    },

    methods: {

      createTransaction: function(){

        let groupId = "98b79ca5-3d1f-466e-a816-1f3fdd3b53a7"
        var transaction = {
          "amount": 900,
          "infoName": "A very expensive Bath",
          "infoLocation": {
            "longitude": null,
            "latitude": null
          },
          "infoCreatedAt": "2018-01-19T00:59:43.511Z",
          "infoImageUrl": null,
          "paidBy": "a17f3942-7633-48c2-844e-b3c8b5a024a1",
          "split": "even"
        };
        axios
          .post(Config.webServiceURL + "/v1/groups/" + groupId + "/transactions", transaction, {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(function(response){
          console.log("Created Transaction: ")
          console.log(response)
        })
        .catch(e => {
          console.log("Errors in POST TRANSACTION: " + e);
          console.log(e)
        });       
      },
    }
  }
</script>