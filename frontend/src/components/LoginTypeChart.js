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
                responsive: true,
                maintainAspectRatio: false,
                //ToDo: Add some specific chart options
                legend: {
                    position: 'bottom',
                    labels: {
                        fontColor: '#BDBDBD'
                    }
                }
            }
        }
    },

    mounted: function() {

        //ToDo: Integrate Options
        //Resolve promise to render chart with desired counts
        console.log("Usercount in LoginTypeChart " + this.userCount)

        this.renderChart({
            labels: ['Password', 'Facebook', 'Google'],
            datasets: [{
                backgroundColor: ['#6E6E6E', '#585858', '#424242'],
                data: [this.passwordUsers, this.facebookUsers, this.googleUsers]
            }, ],
        }, this.options)
    },
})