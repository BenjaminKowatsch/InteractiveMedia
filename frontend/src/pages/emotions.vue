<!-- TEMPLATE for emotions list
  * Name:        Emotions VUE
  * Author:      Daniel Bruckner, Isabeau Schmidt-Nunez
  * Comments:    Isabeau Schmidt-Nunez
  * Description: View to choose one of many suggested emotions in a list in the process of entering one's daily emotion
 -->

<template>
<!-- START Framework 7 Template Elements for establishing the view -->
  <f7-page id="emotions">
   <!-- navigation -->
    <f7-navbar>
      <f7-nav-center sliding>Wie fühlst du dich?</f7-nav-center>
      <!-- Label which asks for the feeling -->
    </f7-navbar>

    <div class="content-block">
    <!-- Begin content -->

      <f7-list>
        <!-- List in which the emotions appear; by clicking one chooses one of these suggestions -->
        <f7-list-item v-for="emotion in emotions" :title="emotion.name" @click="choose(emotion)"></f7-list-item>
      </f7-list>

    </div>
  </f7-page>
  <!-- END of Template Elements -->
</template>

<script>
  /*
  * START JScript Elements for establishing the view according to the template elements
  * imports Config, Axios
  */
  import Mixins from '../mixins.js'
  import Config from '../js/Config.js'
  import Axios from 'axios'


  export default {
    name: 'emotion',
    mixins: [ Mixins ],
    data() {
      return {
        errors: [],
        // defines the suggestions in the list and stores it in an json object
        emotions: []
      }
    },
    mounted: function(){
      this.initEmotions();
    },
    methods: {
      /**
			* If the Page is mounted this function initializes the scrollable emotion list
      * Therefore the checkAsccesToken functions has to succeed
			*/
      initEmotions: function(){
        this.checkAccessToken(this.getEmotionHandler);
      },
      /**
      * Callback function which will be called when the current page is mounted. 
      * This function assigns the response data to the emotions attribute. Therefor a post request (/getEmotion)
      * is sent to the backend to fetch all emotions from the database
      * @param  {JSONObject} accessToken Containing the valid accessToken for the session and an authentication type
			*/
      getEmotionHandler: function(accessToken){
        Axios.post(Config.webServiceURL+`/getEmotion`, accessToken)
        .then(response => {
          this.checkServerResponse(response, payload =>{
            this.emotions = payload
          });
        })
        .catch(e => {
            this.errors.push(e)
        });
      },
      /**
       * Sends the data to backend for further processing
       * @deprecated
       */
      send: function(emotion, color) {
        var json = { "emotion": emotion };
        Axios.post(Config.webServiceURL+'/dataEntry', json).
        then(function(response) {
          console.log('Response Emotion ' + response.data);
          // logs the chosen emotion to the console
        }).
        catch(function(err) {
          console.log(err);
          // logs an error to the console
        })

      },
      /**
			* After the user has choosen an emotion from the scrollable list, he will be redirected to the next page to add a optional diary entry
			* @param  {JSONObject} emotion Emotion-Object containing name and color of the emotion
			*/
      choose: function(emotion) {
        // the function for choosing one of the emotions out of the list
        var mood = this.$f7.mainView.activePage.query.mood;
        // sets the chosen one for the variable mood
        console.log("color: " + emotion.color);
        // logs the color of the chosen emotion to the console
        this.$f7.mainView.router.loadPage('/diary?mood='
                                      + mood + '&emotion='
                                      + emotion.name + '&color='
                                      + emotion.color);
        /*
        *  tells the router to load the next page in the process of entering one's daily mood
        *  and gives the chosen emotion with its color to the next view
        */
      }
    }
  }
</script>
