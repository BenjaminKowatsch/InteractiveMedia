<!-- TEMPLATE for dashboard overview
  * Description: Displays all content for monitoring. Fetch all data from backend and pass to other componens
 -->
<template>
<div>
  <v-container fluid grid-list-md text-xs-center>
    <v-layout row wrap>
      <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn large @click="showUserTable = toggleState(showUserTable)">Overview/Update Users</v-btn>
        <user-table-vuetify v-if="usersLoaded && showUserTable" :users="users" :authToken="authToken"></user-table-vuetify>
      </v-flex>
      <p></p>
      <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn  large @click="showGroupTable = toggleState(showGroupTable)">Overview Groups</v-btn>
        <group-table-vuetify v-if="groupsLoaded && showGroupTable" :groups="groups" :authToken="authToken"></group-table-vuetify>
      </v-flex>
      <p></p>
      <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn  large @click="showLoginTypeChart = toggleState(showLoginTypeChart)">Logintypes</v-btn>
        <login-type-chart v-if="usersLoaded && showLoginTypeChart" :passwordUsers="passwordUsers" :facebookUsers="facebookUsers" :googleUsers="googleUsers"></login-type-chart>
      </v-flex>
      <p></p>
      <v-flex xs12 sm12 md12 lg6 xl6>
        <v-tooltip left>
          <v-btn slot="activator" large @click="showTransactionAmountChart = toggleState(showTransactionAmountChart)">TOP 3 Groups</v-btn>
            <span>Needs at least three groups with transactions</span>
          </v-tooltip>
          <transaction-amout-chart v-if="amountsCalculated && showTransactionAmountChart && transactionGroups.length > 2" :transactionGroups="transactionGroups"></transaction-amout-chart>
      </v-flex>
      <p></p>
      <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn slot="activator" large @click="showResetPW = toggleState(showResetPW)">Reset PW</v-btn>
        <reset-user-pw v-if="usersLoaded && showResetPW" :users="users" :authToken="authToken"></reset-user-pw>
      </v-flex>
      <p></p>
      <v-flex xs12 sm12 md12 lg6 xl6>
        <v-btn slot="activator" large @click="showSearchGroups = toggleState(showSearchGroups)">Search Group</v-btn>
        <search-for-groups v-if="groupsLoaded && showSearchGroups" :groups="groups" :authToken="authToken"></search-for-groups>
      </v-flex>
    </v-layout>    
  </v-container>
 </div>
</template>

<script>
import Mixins from "../mixins.js";
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";
import LoginTypeChart from "../components/LoginTypeChart.js";
import TransactionAmoutChart from "../components/TransactionAmountChart.js";
import UserTableVuetify from "../components/UserTableVuetify.vue";
import GroupTableVuetify from "../components/GroupTableVuetify.vue";
import ResetUserPw from "../components/ResetUserPw.vue";
import SearchForGroups from "../components/SearchForGroups.vue";

export default {
  name: "overview",
  mixins: [Mixins],
  components: {
    LoginTypeChart,
    UserTableVuetify,
    GroupTableVuetify,
    TransactionAmoutChart,
    ResetUserPw,
    SearchForGroups
  },
  
  data() {
    return {
      drawer: false,
      groups: [],
      users: [],
      errors: [],
      authToken: "",
      groupCount: "",
      userCount: "",
      passwordUsers: "",
      facebookUsers: "",
      googleUsers: "",
      transactionGroups: "",      
      showLoginTypeChart: true,
      showTransactionAmountChart: true,
      showUserTable: true,
      showGroupTable: true,
      showResetPW: true,
      showSearchGroups: true,
      groupsLoaded: false,
      usersLoaded: false,
      amountsCalculated: false
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
    // Request initial groups and user data to pass them to other components in the template
    this.getGroups();
    this.getUsers();

  },

  methods: {
    
    // Request all groups from backend
    getGroups: function() {
      axios
        .get(Config.webServiceURL + "/v1/admin/groups", {
          headers: { Authorization: "0 " + this.authToken }
        })
        .then(response => {
          this.groups = response.data.payload;
          this.groupCount = this.groups.length;
          this.transactionGroups = this.groups.filter(this.filter_transactionGroups)  
          console.log("Groups with transactions: " + this.transactionGroups.length)
          this.groupsLoaded = true;
          // If groups with transactions exists, call the prepareTransactionAmounts method
          if(this.transactionGroups.length > 0){
            this.prepareTransactionAmouts()
          }
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in GET admin/groups: " + e);
        });
    },

    // Make a promise to receive the totalAmount of all transactions of each group and sort them in descending order
    prepareTransactionAmouts() {
        return Promise.all(this.transactionGroups.map((group, i) => 
        		this.calculateTransactionAmounts(group.groupId)
            .then(promiseData => group.totalAmount = promiseData)
        )).then(results => {
            this.transactionGroups.sort(this.GetSortOrder("totalAmount"));
            console.log("Sorted Transactiongroups")
            console.log(JSON.stringify(this.transactionGroups))
            this.amountsCalculated = true;
        });
    },

    // Request all transactions for each group with transactions and calculate the total amount of money spending of these transactions
    calculateTransactionAmounts(groupId) {
        return axios.get(Config.webServiceURL + "/v1/admin/groups/" + groupId, {
            headers: {
                Authorization: "0 " + this.authToken
            }
        })
        .then(response => 
        		response.data.payload.transactions.reduce((total, { amount }) => total + amount, 0)
        );
    },

    // Sort the properties of an json-array in descending order (i.e. the total amount of money spending of transactions)
    GetSortOrder: function(prop) {  
        return function(a, b) {  
            if (a[prop] < b[prop]) {  
                return 1;  
            } else if (a[prop] < b[prop]) {  
                return -1;  
            }  
            return 0;  
        }  
      },

    // Request all users from the backend
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
          this.usersLoaded = true;
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors in GET admin/users: " + e);
        });
    },

    //filters groups for transactions
    filter_transactionGroups: function(groups) {
      return groups.countTransactions > 0;
    },

    //filters users object for loginType = Password
    filter_loginPassword: function(users) {
      return users.authType == 0;
    },

    //filters users object for loginType = Google
    filter_loginGoogle: function(users) {
      return users.authType == 1;
    },

    //filters users object for loginType = Facebook
    filter_loginFacebook: function(users) {
      return users.authType == 2;
    },



    //Toggles state of boolean variables
    toggleState: function(state) {
      if (state) {
        return (state = false);
      } else {
        return (state = true);
      }
    },
  }
};
</script>
