<template>
  <v-container>
      <h2>Overview Users</h2>
      <v-spacer></v-spacer>
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
<!--         <td>
          <v-edit-dialog
            lazy
          > {{ props.item.name }}
            <v-text-field
              slot="input"
              label="Edit"
              v-model="props.item.name"
              single-line
              counter
              :rules="[max25chars]"
            ></v-text-field>
          </v-edit-dialog>
        </td> -->
        <td class="text-xs-right">{{ props.item.username }}</td>
        <td class="text-xs-right">{{ props.item.email }}</td>
        <td class="text-xs-right">{{ props.item.userId }}</td>
        <td class="text-xs-right">{{ props.item.authType }}</td>
        <td class="text-xs-right">{{ props.item.countGroupIds }}</td>
      </template>
      <template slot="no-data">
      <v-alert :value="true" color="error" icon="warning">
        Sorry, nothing to display here :(
      </v-alert>
    </template>
     <template slot="footer">
        <td class="text-xs-left" colspan="100%">
          <strong>Total Users: {{ items.length }}</strong>
        </td>
      </template>
      <template slot="pageText" slot-scope="{ pageStart, pageStop }">
        From {{ pageStart }} to {{ pageStop }}
      </template>
    </v-data-table>
  </v-container>
</template>

<script>
 
  export default {

    props: ['users'],

    data () {
      return {
        tmp: '',
        search: '',
        pagination: {},
        headers: [
          { text: 'Username', value: 'username' },
          { text: 'Email', value: 'email', align: 'center'},
          { text: 'User ID', value: 'userId', align: 'center', sortable: false},
          { text: 'Logintype', value: 'authType' },
          { text: 'Groups', value: 'countGroupIds' },
        ],
        items: this.users
      }
    },

    mounted: function(){

        
    }
  }
</script>