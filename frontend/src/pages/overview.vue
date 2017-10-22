<!-- TEMPLATE for emotion overview with moodslider
  * Name:        Overview VUE
  * Author:      Daniel Bruckner, Isabeau Schmidt-Nunez
  * Comments:    Isabeau Schmidt-Nunez
  * Description: View to slide the level of one's actual emotion in the process of entering one's daily emotion
 -->

<template>
<!-- START Framework 7 Template Elements for establishing the view -->
  <f7-page toolbar-fixed>
  <!-- navigation -->
    <f7-navbar>
      <f7-nav-center sliding>Wie ist deine Stimmung heute?</f7-nav-center>
      <!-- label which asks for the actual feeling -->
    </f7-navbar>

    <f7-popup>
      Bitte etwas eintragen
      <!-- popup which apears if one doesn't enter anything -->
    </f7-popup>

    <v-touch class="outerMoodslider" v-on:panstart="start($event)" v-on:panmove="move($event)" v-on:panend="end($event)" v-bind:pan-options="{ direction: 'vertical' }">
      <moodslider></moodslider>
    </v-touch>
    <!-- area for import of the moodslider component -->
    <f7-list form>
      <f7-button id="launeButton" v-on:click="navigate()">...</f7-button>
      <!-- Emotion button -->
    </f7-list>
  </f7-page>
    <!-- END of Template Elements -->
</template>

<script>
 /*
  * START JScript Elements for establishing the view according to the template elements
  * imports Moodslider (to be found in component folder), Axios
  */
  import Moodslider from '@/components/moodslider'
  import Axios from 'axios'


  export default {
    name: 'overview',
    components: {
      Moodslider
      //adds the moodslider as component
    },

    data() {
      return {
        moodbarHeight: 0,
        topOffset: 0,
        progressbar: {},
        laune: 0,
        // sets the variables and defines them
      }
    },
    mounted: function() {
      // loads the moodslider as moodbar and gets template elements by their classes
      var moodbar = document.getElementsByClassName('outerMoodslider')[0];
      this.moodbarHeight = moodbar.clientHeight;
      this.topOffset = moodbar.getBoundingClientRect().top;
      this.progressbar = document.getElementsByClassName('inner')[0];
    },
    methods: {

      /**
       * Handles Touch Event when starting touch
       * All these events set the height of the progressbar
       * according to the touch position
       */

      start: function(event) {

        this.progressbar.setAttribute("style", "height:" +
          this.getProgressBarHeightInPercent(event.center.y) + "%;");
        // raises the height when starting to slide the moodbar via percentage
      },

      /**
       * Triggered when moving finger
       *
       * @param  event  event   The event
       */
      move: function(event) {
        this.progressbar.setAttribute("style", "height:" +
          this.getProgressBarHeightInPercent(event.center.y) + "%;");
        // raises the height while sliding the moodbar via percentage
      },

      /**
       * Triggered when touch gesture ended
       *
       * @param      {<type>}  event   The event
       */
      end: function(event) {
        this.progressbar.setAttribute("style", "height:" +
        this.getProgressBarHeightInPercent(event.center.y) + "%;");
        this.laune = this.getProgressBarHeightInPercent(event.center.y);
        /* 
        *  raises the height until stopping to slide the moodbar 
        *  via percentage and gets the actual level as variable
        */
      },

      /**
       * Gets the progress bar height in percent.
       * This way the moodbar height can be calculated
       *
       * @param      {number}  yCoordinate  The y coordinate used to calculate the
       * percentage
       * @return     {number}  The progress bar height in percent.
       */
      getProgressBarHeightInPercent: function(yCoordinate) {
        var absoluteCoordinate = yCoordinate - this.topOffset;
        var percentage = 100 * (1 - (absoluteCoordinate / this.moodbarHeight));
        // computes the percentage via moodbar height
        this.setButtonLabel(percentage);
        // sets the percentage of the height as label of the button
        return percentage;
      },

      /**
       * Sets the button label for each mood according to height.
       *
       * @param      {number}  percentage  The percentage
       */

      setButtonLabel: function(percentage) {
        // sets the label of the mood button
        var moods = ['Miserabel', 'Schlecht', 'Heiter bis wolkig', 'Gut', 'Überragend'];
        var button = document.getElementById('launeButton');
        /* 
        *  sets an array of possible moods and sets the template element 
        *  emotion button as button variable
        */

        if (percentage >= 0 && percentage <= 20) {
          button.innerHTML = moods[0];
        } else if (percentage >= 20 && percentage <= 40) {
          button.innerHTML = moods[1];
        } else if (percentage >= 40 && percentage <= 60) {
          button.innerHTML = moods[2];
        } else if (percentage >= 60 && percentage <= 80) {
          button.innerHTML = moods[3];
        } else if (percentage >= 80 && percentage <= 100) {
          button.innerHTML = moods[4];
        }
        /*
        *  selects a mood out of the array according to the percentage 
        *  of the chosen emotion level
        */
      },

      /**
       * Checks if a mood has been selected
       * then navigates the user to the emotions view.
       *
       * @return the correct view String.
       */
      navigate: function() {

        var framework7 = new Framework7();
        var button = document.getElementById('launeButton');
        // gets the emotion button by its id out of the template area and sets it to variable button
        if (button != null) {
          var buttonText = button.innerHTML;
          if (buttonText !== '...') {

            console.log(this.laune);
            // logs the mood to the console

            this.$f7.mainView.router.loadPage('/emotion?mood=' +
              this.laune);
            // loads next view and hands over the mood variable

          } else {
            framework7.alert('Bitte trage etwas ein...');
            // sends an alert to the user when nothing is chosen on the slider
          }
        }

      }

    }
  }
</script>
