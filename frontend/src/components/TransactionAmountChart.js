/* TEMPLATE for TransactionAmountChart
Description: Table to display the three most valued transaction groups.
Only shown if there are at least three groups with transactions in the database */
import { Bar } from 'vue-chartjs'

export default {
    extends: Bar,
    props: ['transactionGroups'],

    data() {
        return {
            chartData: {
                labels: [this.transactionGroups[0].name, this.transactionGroups[1].name, this.transactionGroups[2].name],
                datasets: [{
                    label: 'Total transaction amount',
                    backgroundColor: ['#424242', '#585858', '#6E6E6E'],
                    data: [this.transactionGroups[0].totalAmount, this.transactionGroups[1].totalAmount, this.transactionGroups[2].totalAmount]
                }]

            },
            chartOptions: {

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

    mounted() {

        // Renders the chart with the desired chartData and chartOptions (shown above)
        this.renderChart(this.chartData, this.chartOptions)
    }
}