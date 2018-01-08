import { Pie } from 'vue-chartjs';
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

export default Pie.extend({
    name: "PieCount",

    data() {
        return {
            options: {
                //ToDo: Add some specific chart options
            }
        }
    },

    mounted: function() {

        this.authToken = Cookie.getJSONCookie("accessToken").accessToken;

        //Resolve promise to render chart with desired counts
        Promise.all([this.getUsers(), this.getGroups()]).then(promisData => {
            let usersData = promisData[0];
            let groupData = promisData[1];
            console.log(groupData);
            console.log(usersData);

            this.renderChart({
                labels: ['Users', "Groups"],
                datasets: [{
                    backgroundColor: ['#FC2525', '#05CBE1'],
                    data: [usersData, groupData]
                }, ],
            }, { responsive: true, maintainAspectRatio: false }, )
        });
    },
    methods: {

        //Request all groups within an asyc. call to get number of groups
        getGroups: function() {
            return new Promise((resolve, reject) => {
                axios
                    .get(Config.webServiceURL + "/v1/admin/groups", {
                        headers: { Authorization: "0 " + this.authToken }
                    })
                    .then(response => {
                        let groups = response.data.payload;
                        let count = groups.length;
                        console.log("COUNT Groups: " + count)
                        console.log("Gruppen in PieCjjarts: ");
                        console.log(groups);
                        resolve(count)
                    })
                    .catch(e => {
                        console.log("Errors in GET admin/groups: ");
                        console.log(e);
                        reject(e);
                    });
            })
        },

        //Request all users within an asyc. call to get number of users
        getUsers: function() {
            return new Promise((resolve, reject) => {
                axios
                    .get(Config.webServiceURL + "/v1/admin/users", {
                        headers: { Authorization: "0 " + this.authToken }
                    })
                    .then(response => {
                        let users = response.data.payload;
                        let count = users.length;
                        console.log("COUNT Users: " + count)
                        console.log("Users in PieChart: ");
                        console.log(users);
                        resolve(count);
                    })
                    .catch(e => {
                        console.log("Errors in GET admin/users: ");
                        console.log(e);
                        reject(e);
                    });
            })
        },
    }
})