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

        this.renderChart({
            labels: ['Password', 'Facebook', 'Google'],
            datasets: [{
                backgroundColor: ['#6E6E6E', '#585858', '#424242'],
                data: [this.passwordUsers, this.facebookUsers, this.googleUsers]
            }, ],
        }, this.options)
    },
})