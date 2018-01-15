<template>
  <v-card>
    <v-card-title>
      <h2>Overview Groups</h2>
      <v-spacer></v-spacer>
      <v-text-field
        append-icon="search"
        label="Search"
        hide-details
        v-model="search"
      ></v-text-field>
    </v-card-title>
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
        <td class="text-xs-right">{{ props.item.countUsers }}</td>
        <td class="text-xs-right">{{ props.item.countTransactions }}</td>
      </template>
      <template slot="no-data">
      <v-alert :value="true" color="error" icon="warning">
        Sorry, nothing to display here :(
      </v-alert>
    </template>
      <template slot="pageText" slot-scope="{ pageStart, pageStop }">
        From {{ pageStart }} to {{ pageStop }}
      </template>
    </v-data-table>
  </v-card>
</template>

<script>
 
  export default {

    props: ['groups'],

    data () {
      return {
        tmp: '',
        search: '',
        pagination: {},
        headers: [
          { text: 'Groupname', value: 'name' },
          { text: 'Group ID', value: 'groupId', align: 'center', sortable: false},
          { text: 'Date of creation', value: 'createdAt', align: 'center'},
          { text: 'Users', value: 'countUsers' },
          { text: 'Transactions', value: 'countTransactions' },
        ],
        items: this.groups
      }
    },

    mounted: function(){

        
    }
  }
</script>