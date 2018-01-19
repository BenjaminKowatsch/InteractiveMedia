<!-- TEMPLATE for user table
  * Description: Table to display all users and update specific props of them
 -->
<template>
  <v-container>
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
         <td>
          <v-edit-dialog
            lazy
            @open="username = props.item.username,  tmp = props.item, tmpItem = props.item.username"
            @cancel="cancelEditUsername(tmp)"
            @save="userId = props.item.userId, isUsername=true, dialogUsername=true"
          
          > {{ props.item.username }}
            <v-text-field
              slot="input"
              label="Edit"
              v-model="username"
              single-line
              counter              
            ></v-text-field>          
          </v-edit-dialog>
        </td> 
        <td>
          <v-edit-dialog
            lazy
            @open="email = props.item.email,  tmp = props.item, tmpItem = props.item.email"
            @cancel="cancelEditEmail(tmp)"
            @save="userId = props.item.userId, isEmail=true, dialogEmail=true"
          
          > {{ props.item.email }}
            <v-text-field
              slot="input"
              label="Edit"
              v-model="email"
              single-line
              counter              
            ></v-text-field>          
          </v-edit-dialog>
        </td> 
        <td class="text-xs-right">{{ props.item.userId }}</td>
        <td>
          <v-edit-dialog
            lazy
            @open="role = props.item.role,  tmp = props.item, tmpItem = props.item.role"
            @cancel="cancelEditRole(tmp)"
            @save="userId = props.item.userId, isRole=true, dialogRole=true"

          > {{ props.item.role }}
            <v-text-field
              slot="input"
              label="Edit"
              v-model="role"
              single-line
              counter              
            ></v-text-field>          
          </v-edit-dialog>
        </td> 
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
          <v-tooltip bottom>
            <v-btn icon slot="activator">
              <v-icon>fa-question-circle</v-icon>
            </v-btn>
            <span>Update properties by clicking on them (username, email, role)</span>
          </v-tooltip>
        </td>
      </template>
      <template slot="pageText" slot-scope="{ pageStart, pageStop }">
        From {{ pageStart }} to {{ pageStop }}
      </template>
    </v-data-table>
    <v-snackbar
          :timeout="3000"
          :bottom="true"
          class="green darken-4"
          v-model="successFulUpdate"
        >
          Update successful
          <v-btn dark flat @click.native="successFulUpdate = false">X</v-btn>       
    </v-snackbar>
      <v-snackbar
        :timeout="3000"
        :bottom="true"
        class="red darken-4"
        v-model="errorInUpdate"
      >
        Error in update attempt
       <v-btn dark flat @click.native="errorInUpdate = false">X</v-btn>       
      </v-snackbar>
        <v-snackbar
        :timeout="3000"
        :bottom="true"
        class="red darken-4"
        v-model="dontDoThis"
      >
        You really shouldn't do this...
       <v-btn dark flat @click.native="dontDoThis = false">X</v-btn>       
      </v-snackbar>
      <v-dialog v-model="dialogUsername" max-width="500px">
        <v-card>
          <v-card-title>
            <span>Update username to {{username}}?</span>
            <v-spacer></v-spacer>
            <v-btn color="primary" flat @click.stop="updateUser(username), dialogUsername=false">
              Submit
            </v-btn>
            <v-btn color="primary" flat @click.stop="dialogUsername=false, cancelEditUsername(tmp)">Cancel</v-btn>
          </v-card-title>
        </v-card>
      </v-dialog>
      <v-dialog v-model="dialogEmail" max-width="500px">
        <v-card>
          <v-card-title>
            <span>Update email to {{email}}?</span>
            <v-spacer></v-spacer>
            <v-btn color="primary" flat @click.stop="updateUser(email), dialogEmail=false">
              Submit
            </v-btn>
            <v-btn color="primary" flat @click.stop="dialogEmail=false, cancelEditEmail(tmp)">Cancel</v-btn>
          </v-card-title>
        </v-card>
      </v-dialog>
      <v-dialog v-model="dialogRole" max-width="500px">
        <v-card>
          <v-card-title>
            <span>Update role to {{role}}?</span>
            <v-spacer></v-spacer>
            <v-btn color="primary" flat @click="updateUser(role), dialogRole=false">
              Submit
            </v-btn>
            <v-btn color="primary" flat @click.stop="dialogRole=false, cancelEditRole(tmp)">Cancel</v-btn>
          </v-card-title>
        </v-card>
      </v-dialog>
  </v-container>
  
</template>

<script>
 
  import axios from "axios";
  import Config from "../js/Config.js";

  export default {

    props: ['users', 'authToken'],

    data () {
      return {
        tmp: '',
        tmpItem: '',
        search: '',
        pagination: {},
        headers: [
          { text: 'Username', value: 'username' },
          { text: 'Email', value: 'email', align: 'center'},
          { text: 'User ID', value: 'userId', align: 'center', sortable: false},
          { text: 'Role', value: 'role' },
          { text: 'Logintype', value: 'authType' },
          { text: 'Groups', value: 'countGroupIds' },
        ],
        items: this.users,
        isUsername: false,
        isPassword: false,
        isEmail: false,
        isImageUrl: false,
        isRole: false,
        username: "",
        password: "",
        email: "",
        imageUrl: "",
        role: "",
        userId: "",
        ownUserId: "",        
        successFulUpdate: false,
        errorInUpdate: false,
        dontDoThis: false,
        dialogUsername: false,
        dialogEmail: false,
        dialogRole: false
      }
    },


    mounted: function(){

        this.getOwnId()

    },

    methods: {

      // Get own userId for comparison if updating user role of his own user 
      getOwnId: function (){

        axios
        .get(Config.webServiceURL + "/v1/users/user", {
          headers: { Authorization: `0 ${this.authToken}` }
        })
        .then(response => {
          this.ownUserId = response.data.payload.userId;
        })
        .catch(e => {              
              console.log("Errors own user request (UserTableVuetify): " + e);           
            });
      },

      // Trys to update a specific user property
      updateUser: function(prop){

          console.log("Hello Updatefunction")
          var credentials = ""

          if(this.isUsername)
          {
            this.credentials = { username: prop}
            console.log(JSON.stringify(this.credentials))
            this.isUsername = false

            axios.put(Config.webServiceURL + "/v1/admin/users/" + this.userId, this.credentials, {
                headers: { Authorization: "0 " + this.authToken }
              })
              .then(response => 
                  console.log(JSON.stringify(response)),               
                  this.successFulUpdate = true                 
              ).then(
                console.log(this.tmp.username),
                this.saveEditUsername(this.tmp)
              )
              .catch(e => {
                console.log("Errors update username: " + e);
                this.errorInUpdate = true
                console.log(this.tmpItem)
                this.tmp.username = this.tmpItem
              });
          }

          if(this.isEmail)
          {
            this.credentials = { email: prop}
            console.log(JSON.stringify(this.credentials))
            this.isEmail = false

            axios.put(Config.webServiceURL + "/v1/admin/users/" + this.userId, this.credentials, {
                headers: { Authorization: "0 " + this.authToken }
              })
              .then(response => 
                  console.log(JSON.stringify(response)),               
                  this.successFulUpdate = true                 
              ).then(
                console.log(this.tmp.email),
                this.saveEditEmail(this.tmp)
              )
              .catch(e => {
                console.log("Errors update email: " + e);
                this.errorInUpdate = true
                console.log(this.tmpItem)
                this.tmp.email = this.tmpItem
              });
          }

          if(this.isRole)
          {
            console.log("Own: " + this.ownUserId)
            console.log("Other: " + this.userId)

            // Noobfilter to prevent admin to revoke his own admin rights...
            if(this.userId == this.ownUserId && prop == "user"){
              this.dontDoThis = true
              this.cancelEditRole(this.tmp)
            }
            else
            {
              this.credentials = { role: prop}
              console.log(JSON.stringify(this.credentials))
              this.isRole = false

              axios.put(Config.webServiceURL + "/v1/admin/users/" + this.userId, this.credentials, {
                  headers: { Authorization: "0 " + this.authToken }
                })
                .then(response => 
                    console.log(JSON.stringify(response)),               
                    this.successFulUpdate = true                 
                ).then(
                  this.saveEditRole(this.tmp)
                )
                .catch(e => {
                  console.log("Errors update role: " + e);
                  this.errorInUpdate = true
                  console.log(this.tmpItem)
                  this.tmp.role = this.tmpItem
                });
              }
            }
          },

    saveEditUsername(row) {
      row.username = this.username
    },
    

    cancelEditUsername(row) {
      this.username = ''
    },


    saveEditEmail(row) {
      row.email = this.email
    },

    cancelEditEmail(row) {
      this.email = ''
    },


    saveEditRole(row) {
      row.role = this.role
    },

    cancelEditRole(row) {
      this.role = ''
    }
   }
  }
</script>