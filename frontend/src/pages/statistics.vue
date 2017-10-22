<template>
     <f7-page id="emotions">
        <f7-navbar>
      <f7-nav-center sliding>Statistiken</f7-nav-center>
      <f7-nav-right>
        <f7-link icon="icon-bars" open-panel="right"></f7-link>
      </f7-nav-right>
    </f7-navbar>
     <v-touch @swipeleft="increase()" @swiperight="decrease()">
     <!--Call functions if user swipes left or right -->
        <div class="content-block tablet-inset">
          <div class="content-block-inner">
            <div class="small">
              <line-chart :chart-data="datacollection" :options="{responsive: true, maintainAspectRatio: false}"></line-chart>
            </div>
          </div>
        </div>
      </v-touch>
      <f7-buttons>
        <f7-button v-on:click='decrease()'>Zurück</f7-button>
        <f7-button  v-on:click='increase()'>Vor</f7-button>
      </f7-buttons>
  </f7-page>
</template>

<script>
import Mixins from '../mixins.js'
import Config from '../js/Config.js'
import LineChart from '../js/LineChart.js'
import axios from 'axios'

  export default {
    mixins: [ Mixins ],
    components: {
      LineChart
    },
    data () {
      return {
        errors: [],
        datacollection:  {},
        min:0,
        max:0
      }
    },
    mounted () {
      this.getMaxVal();
      this.getStatistics();
    },
    methods: {
      /**
       * Fetches all statistical data from the logged in user and assigns the number of entries to the max-value attribute.
       * => Necessary to set an upper limit
       * @param  {JSONObject} accessToken Containing the valid accessToken for the session and an authentication type
       */
      getMaxValHandler: function(accessToken){

        axios.post(Config.webServiceURL+`/get_max_val`, accessToken)
        .then(response => {
          this.checkServerResponse(response, payload =>{
            if(payload[0]){
              this.max = payload[1];
            }else{
              console.log(payload[1]);
            }
          });
        })
        .catch(e => {
            this.errors.push(e)
        });
      },

      /**
      * This function is called when the page is mounted and tries to fetch the number of data entries from the database
      */
      getMaxVal: function(){
        this.checkAccessToken(this.getMaxValHandler);
      },
      /**
      * Callback function which is called when the page is initialized
      * Adds the minimum value to the payload of the accessToken. Subsequently the accessToken will be send to the backend
      * The Response data is used to fill the dataCollection array
      * @param  {JSONObject} accessToken Containing the valid accessToken for the session and an authentication type
      */
      getStatisticsHandler: function(accessToken){
        accessToken.payload = {};
        accessToken.payload.min = this.min;
        axios.post(Config.webServiceURL+`/get_stat`, accessToken)
        .then(response => {
            // JSON responses are automatically parsed.
            if(true === response.data.success){
              var payload = response.data.payload;
              if(payload[3]){
                this.datacollection = {
                          labels: payload[0],
                          datasets: [{
                            label: 'Launen',
                            backgroundColor: payload[1],
                            data: payload[2]
                          }]
                        };
              } else {
                f7.alert('Keine Daten vorhanden', 'Launometer');
              }
            } else {
              console.log("Invalid accessToken");
            }
        })
        .catch(e => {
            this.errors.push(e)
        });
      },

      /**
      * This function is called when the page is mounted and calls the getStatisticsHandler method to 
      * fetch all statistical data from the backend
      * Therefore the checkAsccesToken functions has to succeed
      */
      getStatistics: function(){
        this.checkAccessToken(this.getStatisticsHandler);
      },

      /**
      * If the user swipes left or clicks the 'vor' button, min value is increased and the getStatistics method
      * is called with a new min value
      * => Pagination
      *
      */
      increase: function (){
         if(this.min<(this.max -5)){
            this.min = this.min + 5;
            this.getStatistics();
        }else{
          f7.alert('Es liegen keine aktuelleren Daten vor', 'Launometer');
        }
      },

      /**
      * If the user swipes right or clicks the 'zurück' button, min value is decreased and the getStatistics method
      * is called with a new min value
      * => Pagination
      */
      decrease: function (){
        if(this.min!=0){
          this.min = this.min - 5;
          this.getStatistics();
        }else{
          f7.alert('Für einen früheren Zeitpunkt liegen keine Daten vor', 'Launometer');
        }
      }
    }
  }

</script>
