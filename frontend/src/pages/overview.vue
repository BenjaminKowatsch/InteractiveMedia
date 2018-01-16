<!-- TEMPLATE for dashboard overview
  * Description: Displays all content for monitoring. Fetch all data from backend and pass to other componens
 -->

<template>
<div>
  <v-container fluid grid-list-md text-xs-center>
    <v-layout row wrap>
    <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn large @click="showUserTable = toggleState(showUserTable)">Overview Users</v-btn>
        <v-spacer></v-spacer>
        <user-table-vuetify v-if="usersLoaded && showUserTable" :users="users"></user-table-vuetify>
    </v-flex>
    <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn large @click="showGroupTable = toggleState(showGroupTable)">Overview Groups</v-btn>
        <group-table-vuetify v-if="groupsLoaded && showGroupTable" :groups="groups"></group-table-vuetify>
    </v-flex>
    <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn large @click="showLoginTypeChart = toggleState(showLoginTypeChart)">Logintypes</v-btn>
        <v-spacer></v-spacer>
        <login-type-chart v-if="usersLoaded && showLoginTypeChart" :passwordUsers="passwordUsers" :facebookUsers="facebookUsers" :googleUsers="googleUsers"></login-type-chart>
    </v-flex>
    </v-layout>
</v-container>

<!--         <input type="button" v-on:click="createDummyGroup()" value="AddDummyGroup"/>
        <input type="button" v-on:click="showGroupUserChart = toggleState(showGroupUserChart)" value="Show User and Groups Chart"/>

        <input type="button" v-on:click="showLoginTypeChart = toggleState(showLoginTypeChart)" value="Show Logintype Chart"/>
        <input type="button"  v-on:click="logout()" value="Logout"/> -->
                    
<!--         <div class="loginTypeChart">
          <div v-if="usersLoaded && showLoginTypeChart">
            <login-type-chart :passwordUsers="passwordUsers" :facebookUsers="facebookUsers" :googleUsers="googleUsers"></login-type-chart>
          </div> 
        </div>     -->          
<!--       <div class="version" v-if="version">
        <p>Debts² admin panel version informations: 
          {{version.name}}  {{version.version}}
        </p>
      </div> -->
 </div>
</template>

<script>
import Mixins from "../mixins.js";
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";
import LoginTypeChart from "@/components/LoginTypeChart.js";
import UserTableVuetify from "@/components/UserTableVuetify.vue";
import GroupTableVuetify from "@/components/GroupTableVuetify.vue";



export default {
  name: "overview",
  mixins: [Mixins],
  components: {
    LoginTypeChart,
    UserTableVuetify,
    GroupTableVuetify
  },
  
  data() {
    return {
      drawer: false,
      version: [],
      groups: [],
      users: [],
      errors: [],
      authToken: "",
      groupId: "",
      userId: "",
      groupCount: "",
      userCount: "",
      passwordUsers: "",
      facebookUsers: "",
      googleUsers: "",
      showLoginTypeChart: true,
      showUserTable: true,
      showGroupTable: true,
      groupsLoaded: false,
      usersLoaded: false,
    };
  },

  mounted: function() {

    this.authToken = Cookie.getJSONCookie("accessToken").accessToken;
    console.log("The cookie authToken is: " + this.authToken);

    // if no accessToken is set or if something went wrong, redirect user to loginpage
    if(this.authToken == "undefined" || this.authToken == undefined || this.authToken == null){
      this.redirect("/");
    }

    this.groups = [];
    this.users = [];
    this.groupId = "9a7fb2f3-8b39-4849-ac81-48c1835220d0";
    this.userId = "8b8901fb-4129-4e85-a910-2a1cba922bbf";

    this.authorizeAdmin();
    this.getGroups();
    this.getGroupById(this.groupId);
    this.getUsers();
    this.getUserById(this.userId);
    this.getVersionInfos();

  },

  methods: {
    /*     Create a dummy group for testpurpose. After creating, page has to be reloaded to see group*/
    /* createDummyGroup: function() {
      axios
        .post(
          Config.webServiceURL + "/v1/groups",
          {
            name: "Testgroup3",
            imageUrl: null,
            users: ["admin@example.com"]
          },
          {
            headers: { Authorization: `0 ${this.authToken}` }
          }
        )
        .then(function(response) {
          console.log(response);
        })
        .catch(function(error) {
          console.log(error);
        });
    }, */

    authorizeAdmin: function() {
      axios
        .get(Config.webServiceURL + "/v1/test/authorization/admin", {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(function(response) {
          console.log("Authorization as admin: " + response.data.payload.hello);
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in admin authorization: " + e);
        });
    },

    getGroups: function() {
      axios
        .get(Config.webServiceURL + "/v1/admin/groups", {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {
          this.groups = response.data.payload;
          this.groupCount = this.groups.length;
          // this.groupCountLoaded = true;
          console.log("Anzahl Gruppen: " + this.groupCount);
          this.groupsLoaded = true;
          // console.log("Existing Groups: " + JSON.stringify(this.groups));
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in GET admin/groups: " + e);
        });
    },

    getGroupById: function(id) {
      axios
        .get(Config.webServiceURL + "/v1/admin/groups/" + id, {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(function(response) {
          console.log(
            "Desired Group: " + JSON.stringify(response.data.payload)
          );
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in GET admin/groups/:groupID: " + e);
        });
    },

    getUsers: function() {
      axios
        .get(Config.webServiceURL + "/v1/admin/users", {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {

          this.users = response.data.payload;
          this.userCount = this.users.length;

          this.passwordUsers = this.users.filter(this.filter_loginPassword).length
          this.facebookUsers = this.users.filter(this.filter_loginFacebook).length
          this.googleUsers = this.users.filter(this.filter_loginGoogle).length

          console.log("Count Users: " + this.userCount);
          console.log("Count PasswordUsers: " + this.passwordUsers);
          console.log("Count FacebookUsers: " + this.facebookUsers);
          console.log("Count GoogleUsers: " + this.googleUsers);

          this.usersLoaded = true;
          // console.log("Existing Users: " + JSON.stringify(this.users));
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in GET admin/users: " + e);
        });
    },

    getUserById: function(id) {
      axios
        .get(Config.webServiceURL + "/v1/admin/users/" + id, {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {
          console.log("Desired User: " + JSON.stringify(response.data.payload));
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in GET admin/users/:userID : " + e);
        });
    },

    getVersionInfos: function() {
      axios
        .get(Config.webServiceURL + "/v1/version")
        .then(response => {
          // JSON responses are automatically parsed.
          this.version = response.data;
          console.log("Version: " + response.statusText);
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in Version: " + e);
        });
    },

    //Counts the elements of an object4
    //ToDo: Delete Fct if not used
    countProperties: function(obj) {
      var count = 0;
      for (var property in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, property)) {
          count++;
        }
      }
      return count;
    },

    //filters users object for loginType = Password
    filter_loginPassword: function(users) {
      return users.authType == 0;
    },

    //filters users object for loginType = Facebook
    filter_loginFacebook: function(users) {
      return users.authType == 1;
    },

    //filters users object for loginType = Google
    filter_loginGoogle: function(users) {
      return users.authType == 2;
    },

    //Toggles state of boolean variables
    toggleState: function(state) {
      console.log(state);
      if (state) {
        return (state = false);
      } else {
        return (state = true);
      }
    },

    //Logout the current user
  /*   logout: function() {
      let accessToken = this.authToken;

      //Check for existing accessToken
      this.checkAccessToken(accessToken => {
        console.log("AuthToken in checkAccess fct: " + this.authToken);
        //Post data to the backend to successfully logout the user and redirect to login page
        axios
          .post(Config.webServiceURL + `/v1/users/logout`, {
            headers: {
              Authorization: "0 " + this.authToken
            }
          })
          .then(response => {
            console.log(JSON.stringify(response.data));
            Cookie.deleteCookie("accessToken");
            this.redirect("/", false, false, true);
          })
          .catch(e => {
            console.log(JSON.stringify(e));
          });
      });
    } */
  }
};
</script>
