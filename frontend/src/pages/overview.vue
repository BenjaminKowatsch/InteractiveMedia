<!-- TEMPLATE for emotion overview with moodslider
  * Name:        Overview VUE
  * Author:      Daniel Bruckner, Isabeau Schmidt-Nunez
  * Comments:    Isabeau Schmidt-Nunez
  * Description: View to slide the level of one's actual emotion in the process of entering one's daily emotion
 -->

<template>
<!-- START Framework 7 Template Elements for establishing the view -->
  <f7-page toolbar-fixed>
    <f7-navbar>
      <f7-nav-center sliding>Dashboard</f7-nav-center>
    </f7-navbar>
    <f7-list form>

      <h2>Groups overview</h2> 
      <div v-if="groupCount > 0">
        <li v-for="group in groups">
          <p><b>Group name:</b> {{group.name}}, <b>Group id:</b> {{group.groupId}}</p>
          <p><b>User count:</b> {{group.countUsers}}, <b>Transaction count:</b> {{group.countTransactions}}</p>
        </li>
      </div>
      <div v-else>
        <h2>NO GROUPS FOUND</h2>
      </div>

      <div id="groupcount">
         <h3>Number of groups: {{groupCount}}</h3>
      </div>

       <br/>

      <h2>Users overview</h2> 
      <div v-if="userCount > 0">
        <li v-for="user in users">
          <p><b>User name:</b> {{user.username}}, <b>User id:</b> {{user.userId}}, <b>User email:</b> {{user.email}}</p>
          <p><b>User role:</b> {{user.role}}, <b>Group Count:</b> {{user.countGroupIds}}</p>
        </li>
      </div>
      <div v-else>
        <h2>NO USERS FOUND</h2>
      </div>

      <div id="usercount">
         <h3>Number of users: {{userCount}}</h3>
      </div>

      <br/>

      <div id="chart">
        <div v-if="showChart">
                <pie-count></pie-count>
        </div>
      </div>

      <div v-if="version">
        <p>Debts² admin panel version informations: 
          {{version.name}}  {{version.version}}
        </p>
      </div>
        </f7-list form>  
    <f7-list>
      <f7-list-button title="Show Chart" v-on:click="showChart = toggleState(showChart)"></f7-list-button>    
      <f7-list-button title="CreateDummyGroup" v-on:click="createDummyGroup()"></f7-list-button>
      <f7-list-button title="Logout" v-on:click="logout()"></f7-list-button>
    </f7-list>
  </f7-page>
    <!-- END of Template Elements -->
</template>

<script>
import Mixins from "../mixins.js";
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";
import PieCount from "@/components/PieCount.js";

export default {
  name: "overview",
  mixins: [Mixins],
  components: {
    PieCount
  },
  data() {
    return {
      version: [],
      groups: [],
      users: [],
      errors: [],
      authToken: "",
      groupId: "",
      userId: "",
      groupCount: "",
      userCount: "",
      showChart: false
    };
  },

  mounted: function() {
    // var groupCountLoaded = false;
    // console.log(groupCountLoaded);
    this.authToken = Cookie.getJSONCookie("accessToken").accessToken;
    console.log("The cookie authToken is: " + this.authToken);
    this.groups = [];
    this.users = [];
    this.groupId = "9a7fb2f3-8b39-4849-ac81-48c1835220d0";
    this.userId = "facad137-28e7-49a2-a39c-6ecc0c1a7e85";

    this.authorizeAdmin();
    this.getGroups();
    this.getGroupById(this.groupId);
    this.getUsers();
    this.getUserById(this.userId);
    this.getVersionInfos();
  },

  methods: {
    /*     Create a dummy group for testpurpose. After creating, page has to be reloaded to see group*/
    createDummyGroup: function() {
      axios
        .post(
          Config.webServiceURL + "/v1/groups",
          {
            name: "Testgroup0",
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
    },

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
          this.groupCount = this.countProperties(this.groups);
          // this.groupCountLoaded = true;
          console.log("Anzahl Gruppen: " + this.groupCount);
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
          this.userCount = this.countProperties(this.users);
          console.log("Count Users: " + this.userCount);
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

    //Counts the elements of an object
    countProperties: function(obj) {
      var count = 0;
      for (var property in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, property)) {
          count++;
        }
      }
      return count;
    },

    //Toggles state of boolean variables
    toggleState: function(state) {
      console.log("State of showChart before toggle");
      console.log(state);
      if (state) {
        return (state = false);
      } else {
        return (state = true);
      }
    },

    //Logout the current user
    logout: function() {
      let accessToken = this.authToken;

      //Check for existing accessToken
      this.checkAccessToken(accessToken => {
        console.log("AuthToken in checkAccess fct: " + this.authToken);
        //Post data to the backend to successfully logout the user and redirect to login page
        axios
          .post(Config.webServiceURL + `/v1/users/logout`, this.authToken, {
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
    }
  }
};
</script>
