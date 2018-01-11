
<script>
import { Pie } from "vue-chartjs";
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";
//ToDo: Delete if not further needed
export default Pie.extend({
  name: "PieChart",

  props: ['userCount', 'groupCount'],

  watch: {
         'userCount': {
            handler: function(val) {
                console.log("Data in userCount been changed. New UserCount: " + val)
                this._chart.datasets[0].data = [val,]
                this._chart.update();
            },
            deep: true
        },
        'groupCount': {
            handler: function(val) {
                console.log("Data in groupCount been changed. New GroupCount: " + val)
                this._chart.datasets[0].data = [, val]
                this._chart.update();
            },
            deep: true
        }

  },

  data (){

    return {
    options: {
                /* my specific data */
            }
    }
  },
  mounted: function() {
      this.userCount = 3
      this.groupCount = 5
    this.authToken = Cookie.getJSONCookie("accessToken").accessToken;
    /*         this.getGroups();
                this.getUsers(); */

    axios
      .get(Config.webServiceURL + "/v1/admin/groups", {
        headers: { Authorization: "0 " + this.authToken }
      })
      .then(response => {
        this.groups = response.data.payload;
        this.groupCount = this.countProperties(this.groups);
        // this.groupCountLoaded = true;
        console.log("Anzahl Gruppen in PieChart2: " + this.groupCount);
        console.log(
          "Existing Groups in PieCharts2: " + JSON.stringify(this.groups)
        );
      })
      .catch(e => {
        this.errors.push(e);
        console.log("Errors in GET admin/groups: " + error);
      });

    axios
      .get(Config.webServiceURL + "/v1/admin/users", {
        headers: { Authorization: "0 " + this.authToken }
      })
      .then(response => {
        this.users = response.data.payload;
        this.userCount = this.countProperties(this.users);
        console.log("Count Users in PieChart2: " + this.userCount);
        console.log(
          "Existing Users in PieChart2: " + JSON.stringify(this.users)
        );
      })
      .catch(e => {
        this.errors.push(e);
        console.log("Errors in GET admin/users: " + e);
      });

this.renderChart({
            labels: ['Users', "Groups"],
            datasets: [{
                    //label: 'Count',
                    backgroundColor: ['#FC2525', '#05CBE1'],
                    data: [this.userCount, this.groupCount]
                },
            ],
        }, { responsive: true, maintainAspectRatio: false })
      console.log("intial Renderin. Usercount: " + this.userCount + ", Groupcount: " + this.groupCount)
           

   },
  methods: {
    //Counts the elements of an object
    countProperties: function(obj) {
      var count = 0;
      for (var property in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, property)) {
          count++;
        }
      }
      return count;
    }
  }
});
</script>