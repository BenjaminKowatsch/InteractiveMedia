<!-- TEMPLATE for diary entries
	*	Name: 		 Diary VUE
	*	Author: 	 Daniel Bruckner
	*	Comments:	 Isabeau Schmidt-Nunez
	*	Description: View to enter a specific description of one's emotions and thoughts imitating a diary entry. Appears in the 	*				 process of entering one's daily emotion
 -->

<template>
	<!-- START Framework 7 Template Elements for establishing the view -->
	<f7-page toolbar-fixed>
		<!-- navigation -->
		<f7-navbar>
			<f7-nav-center sliding>Beschreibung</f7-nav-center>
			<!-- Label for description of the emotion -->
		</f7-navbar>

		<!-- HTML Elements -->
		<div class="list-block">
			<!-- Text Area wrapped in list element -->
			<ul>
				<!-- Additional "align-top" class for correct textarea alignment -->
				<li class="align-top">
					<div class="item-content">
						<div class="item-inner">
							<div class="item-input">
								<textarea v-model="text" placeholder="Beschreibung zu den vorherigen Auswahlen..."></textarea>
								<!-- Textarea to insert emotions and thoughts with placeholder -->
							</div>
						</div>
					</div>
				</li>

			</ul>

			<f7-button class="buttons" @click="saveEntry(false)">Bestätigen</f7-button>
			<!-- Button to confirm one's entries -->
			<f7-button class="buttons" @click="saveEntry(true)">Überspringen</f7-button>
			<!-- Button to skip this step in the process of entering one's daily emotion -->
		</div>

	</f7-page>

	<!-- END of Template Elements -->
</template>


<script>
	/*
	*	START JScript Elements for establishing the view according to the template elements
	*	imports Mixins, Config, Cookie, Axios
	*/

    import Mixins from '../mixins.js'
	import Config from '../js/Config.js'
	import Axios from 'axios'

	export default {
		name: 'diary',
	  mixins: [ Mixins ],
		data() {
			return {
				date: 0,
				color: '',
				text:'',
				skip: false
				// Sets up variables and configures them
			}
		},
		methods: {
			/**
			* After adding a emtpy payload to the accesToken, this function adds the entered data to the payload attributes. After that the accesToken is sent to the backend (with Axios)
			* @param  {JSONObject} accessToken Containing the valid accessToken for the session and an authentication type
			*/
			saveEntryHandler: function(accessToken){
				accessToken.payload = {};
				// Add payload
				accessToken.payload.date = Math.floor(Date.now());
				// loads the actual date of the system and sets it as diary entry date
				accessToken.payload.mood = Number(this.$f7.mainView.activePage.query.mood);
				accessToken.payload.emotion = this.$f7.mainView.activePage.query.emotion;
				accessToken.payload.colour = '#' + this.$f7.mainView.activePage.query.color;

				if(false === this.skip){
					accessToken.payload.text = this.text;
					// if skip is not true then register text and put it to my variable
				}
				Axios.post(Config.webServiceURL + '/insertData', accessToken)
				.then(response => {
					this.checkServerResponse(response, payload =>{
						console.log("Test: "+ payload);
						// log to console about test run
						this.$f7.mainView.router.loadPage('/statistics');
						// loads the statistics view
					});
				})
				.catch(function(error){
					console.log("Error: "+JSON.stringify(error));
					// error log to console with description
				});
			},
			/**
			* This function checks if a user wants to skip the text entry for the diary. If he skips the text input a empty value is added to the database
			* Therefore the checkAsccesToken functions has to succeed
			* @param  {bool} skip Containing the boolean value for the if statement
			*/
			saveEntry: function(skip) {
				this.skip = skip;
				// Check if text should be skipped or if a text was entered
				if (true === skip || this.text.length !== 0) {
					this.checkAccessToken(this.saveEntryHandler);
					// entered text is saved
				} else {
					f7.alert('Bitte etwas eintragen', 'Launometer');
					// if text should not be skipped but field is empty, one is asked to enter a text
				}
			}
		}
	}
</script>
