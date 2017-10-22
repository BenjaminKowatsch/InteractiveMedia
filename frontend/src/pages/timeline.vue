<template>
    <f7-page id="emotions">
        <f7-navbar>
          <f7-nav-left back-link="Back" sliding></f7-nav-left>
          <f7-nav-center sliding>Timeline</f7-nav-center>
        </f7-navbar>
    <f7-timeline id='line' sides>
    <f7-timeline-item v-for="timeline in entry" side="right" :day="timeline.day" :month="timeline.month" inner :content="timeline.message"></f7-timeline-item>

    </f7-timeline>

  </f7-page>
</template>

<script>

import Mixins from '../mixins.js'
import Config from '../js/Config.js'
import axios from 'axios'

export default {
    name: 'timeline',
    mixins: [ Mixins ],
    data() {
      return {
        errors: [],
        entry: []
      }
    },
    mounted () {
      this.getDiary()
    },
    methods: {
      /**
       * Callback function which will be called when the page is mounted
       * This function sends a post request to the backend and assigns the data from the response to the entry attribute
       * @param  {JSONObject} accessToken Containing the valid accessToken for the session and an authentication type
       */
      getDiaryHandler: function(accessToken){
        axios.post(Config.webServiceURL+`/get_diary`, accessToken)
        .then(response => {
          this.checkServerResponse(response, payload =>{
            if(payload[0]){
              this.entry = payload[1];
            }else{
              f7.alert('Keine Daten vorhanden', 'Launometer');
            }
          });
        })
        .catch(e => {
            this.errors.push(e)
        });
      },
      /**
       * This function is called when the page is mounted and calls the getDiaryHandler function to fetch all entries
       * Therefore the checkAsccesToken functions has to succeed
       */
      getDiary: function(){
        this.checkAccessToken(this.getDiaryHandler);
      }
  }
}
</script>
