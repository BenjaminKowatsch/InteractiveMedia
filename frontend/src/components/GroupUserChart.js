import { Pie } from 'vue-chartjs';
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

export default Pie.extend({
    name: "GroupUserChart",
    props: ['groupCount', 'userCount'],

    data() {
        return {
            options: {
                //ToDo: Add some specific chart options
            }
        }
    },

    mounted: function() {

        //Resolve promise to render chart with desired counts
        console.log("Usercount in PieCount: " + this.userCount)
        console.log("Groupcount in PieCount: " + this.groupCount)

        this.renderChart({
            labels: ['Users', "Groups"],
            datasets: [{
                backgroundColor: ['#FC2525', '#05CBE1'],
                data: [this.userCount, this.groupCount]
            }, ],
        }, { responsive: true, maintainAspectRatio: false }, )
    },
})