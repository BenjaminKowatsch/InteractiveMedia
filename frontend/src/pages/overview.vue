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

      <li v-for="group in groups">
       <p>{{group.name}}</p>
<!--         <p>{{group.users}}</p>      
 -->        </li>
      <div id="groupcount">
<!--         <h2>{{groupCount}}</h2>
 -->      </div>


  <div v-if="groups.length = 0">
    <h1>NO GROUPS FOUND</h1>
  </div>

  <div v-if="version">
    <p>Aktuelle Versionshinweise: 
     {{version.name}}  {{version.version}}
    </p>
  </div>
       </f7-list form>  
    <f7-list>
      <f7-list-button title="CreateDummyGroup" v-on:click="createDummyGroup()"></f7-list-button>
<!--       <f7-list-button title="logout" v-on:click="createDummyGroup()"></f7-list-button>
 -->
    </f7-list>
  </f7-page>
    <!-- END of Template Elements -->
</template>

<script>
import Config from "../js/Config.js";
import axios from "axios";
import Cookie from "../js/Cookie.js";

export default {
  name: "overview",

  data() {
    return {
      version: [],
      groups: "",
      errors: [],
      authToken: "",
      groupCount: ""
    };
  },

  // Fetches posts when the component is created.
  mounted: function() {
    //groupCount = ""
    this.authToken = Cookie.getJSONCookie("accessToken").accessToken;
    console.log("The cookie authToken is: " + this.authToken);

   /*  axios
        .get(Config.webServiceURL + "/v1/users/user", {
          headers: { Authorization: `0 ${this.authToken}` }
        })
        .then(response => {
          var userData = response.data;
          console.log("Response from Userrequest: " + this.userData);
        })
        .catch(e => {
          this.errors.push(e);
          console.log("Errors userrequest: " + e);
        })
 */
    //Gets groupnames. To access all groups, admin access required. For testing specific groupid is used
    //Seltsames: object enthält bei response.data zwei Objecte, da success: true als eingenes object gehandelt wird (bzw nur true)
    //Mögliches unsauberes mocking für gesamtanzahl anzeige: Resultat/2 ...
   /*  axios
      .get(
        Config.webServiceURL +
          "/v1/groups/a43f1597-b23d-4c8e-96d3-e707d8e00d51",
        {
          headers: { Authorization: `0 ${this.authToken}` }
        },
        { responseType: "stream" }
      )
      .then(response => {
        // JSON responses are automatically parsed.
        this.groups = response.data;

        console.log("Groups: " + response.statusText);
        console.log("Headers: " + response.headers);
        console.log("Config: " + response.config);
        console.log("Data: " + response.data);
        console.log("Typ: " + typeof response.data);
        console.log("Objectlength: " + Object.keys(this.groups).length);
        console.log("Groups in array: " + this.groups);
        console.log("Groups in Array JSON: " + JSON.stringify(this.groups));
        console.log(
          "Array with JSONStringy: " + JSON.stringify(this.groups.length)
        );
        console.log("Groupsarray size: " + this.groups.size);
        console.log("Groupsarray length: " + this.groups.length); */
        /* var filteredGroups = groups.filter(function(el) {
          return el.success != true;
        }); */
/*         console.log(
          "Groupsarray after filtering: " + JSON.stringify(this.filteredGroups)
        );
        console.log(
          "Groupsarray count after filtering: " +
            Object.keys(this.filteredGroups).length
        ); */
  /*     })
      .catch(e => {
        this.errors.push(e);
        console.log("Errors in Groups: " + e);
      }); */

    //groupCount = this.countProperties(this.groups);

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

  methods: {
    /*     Create a dummy group for testpurpose. After creating, page has to be reloaded to see group*/
    createDummyGroup: function() {
      axios
        .post(
          Config.webServiceURL + "/v1/groups",
          {
            name: "Testgroup1",
            imageUrl: null,
            users: ["asdfg@web.de", "alexa@web.de"]
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

    countProperties: function(obj) {
      var count = 0;

      for (var property in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, property)) {
          count++;
        }
      }
      return count;
    }
    /* saveToken: function(response){
      console.log("Entered TokenFunction")
      
      this.authToken = response.data.payload.accessToken
      console.log("AuthToken: " + this.authToken)
      this.getAccessToken()
      console.log("THis was the getAccessToken call from Login.vue")

    },

      accessToken: function(){
        console.log("Entered getterMethod")
        this.tokenStr = this.login.getAccessToken()
        console.log("Saved accessToken from login: " + tokenStr)

      }, */
  }
};
</script>
