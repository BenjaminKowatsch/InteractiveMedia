import { Pie } from 'vue-chartjs';
import axios from "axios";
import Cookie from "../js/Cookie.js";
import Config from "../js/Config.js";

export default Pie.extend({
    name: "LoginTypeChart",
    props: ['passwordUsers', 'facebookUsers', 'googleUsers'],

    data() {
        return {
            options: {
                //ToDo: Add some specific chart options
            }
        }
    },

    mounted: function() {


        //Resolve promise to render chart with desired counts
        console.log("Usercount in LoginTypeChart " + this.userCount)

        this.renderChart({
            labels: ['Password', 'Facebook', 'Google'],
            datasets: [{
                backgroundColor: ['#5FB404', '#2E64FE', '#DF0101'],
                data: [this.passwordUsers, this.facebookUsers, this.googleUsers]
            }, ],
        }, { responsive: true, maintainAspectRatio: false }, )
    },
})