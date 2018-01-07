import { Pie } from 'vue-chartjs';
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

export default Pie.extend({
    name: "PieCount",
    props: ['data'],
    /* data() {
        return {
            users: [],
            groups: [],
            userCount: "",
            groupCount: ""
        }
    }, */
    watch: {
        'data': {
            handler: function(newData, oldData) {
                let chart = this._chart

                let newDataLabels = newData.datasets.map((dataset) => {
                    return dataset.label
                })

                let oldDataLabels = oldData.datasets.map((dataset) => {
                    return dataset.label
                })
                if (JSON.stringify(newDataLabels) === JSON.stringify(oldDataLabels)) {
                    newData.datasets.forEach(function(dataset, i) {
                            chart.data.datasets[i].data = dataset.data
                        })
                        //chart.data.labels = newData.labels
                    chart.update()
                } else {
                    this.renderChart(this.data, this.options)
                }
            },
            deep: true
        }
    },

    data() {
        return {
            options: {
                /* my specific data */
            }
        }
    },

    mounted: function() {
        this.userCount = -1
        this.groupCount = -1
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
                console.log("Anzahl Gruppen in PieChart: " + this.groupCount);
                console.log("Existing Groups in PieCharts: " + JSON.stringify(this.groups));
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
                console.log("Count Users in PieChart: " + this.userCount);
                console.log("Existing Users in PieChart: " + JSON.stringify(this.users));
            })
            .catch(e => {
                this.errors.push(e);
                console.log("Errors in GET admin/users: " + e);
            });


        /* this.renderChart({
            labels: ['Users', 'Groups'],
            datasets: [{
                label: 'Users',
                backgroundColor: '#FC2525',
                data: [this.userCount]
            }, {
                label: 'Groups',
                backgroundColor: '#05CBE1',
                data: [this.groupCount]
            }]
        }, { responsive: true, maintainAspectRatio: false }) */
        this.renderChart(this.data, this.options)
        console.log("Userscount in PieCount: " + this.userCount)
        console.log("Groupscount in PieCount: " + this.groupCount)

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
                    console.log("Anzahl Gruppen in PieChart: " + this.groupCount);
                    console.log("Existing Groups in PieCharts: " + JSON.stringify(this.groups));
                })
                .catch(e => {
                    this.errors.push(e);
                    console.log("Errors in GET admin/groups: " + error);
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
                    console.log("Count Users in PieChart: " + this.userCount);
                    console.log("Existing Users in PieChart: " + JSON.stringify(this.users));
                })
                .catch(e => {
                    this.errors.push(e);
                    console.log("Errors in GET admin/users: " + e);
                });
        },
    }


})