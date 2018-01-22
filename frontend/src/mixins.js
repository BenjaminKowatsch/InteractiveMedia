import Cookie from './js/Cookie.js'
export default {
    data() {
        return {}
    },
    methods: {

        /**
         * This function checks if the cookie 'accessToken' exists and passes it to the onValid Callback if it exists.
         * Otherwise it will show an alert to the user and redirect to the login page.
         * This function shall be used before sending user specific data to the backend.
         * @param  {function} onValid Callback function which will be called if the JSON cookie 'accessToken' exists
         */
        checkAccessToken: function(onValid) {
            var accessToken = Cookie.getJSONCookie('accessToken')
            if (accessToken !== '') {
                onValid(accessToken)
            } else {
                this.redirect('/')

            }
        },
        /**
         * This function logs the user in and writes a JSON cookie with the accessToken and the authentication type received from the backend.
         * Afterwards the user will be redirected to the 'main' page '/overview'
         * This function shall be used when logging the user in or registering and directly logging the user in
         * @param  {JSONObject} payload The payload of the response from the backend after sending a login or register request
         */
        loginUser: function(payload) {
            // sets a cookie to a logged in user
            var jsonCookie = { 'accessToken': payload.accessToken, '/* authT */ype': payload.authType }
            Cookie.createJSONCookie('accessToken', jsonCookie) // creates the cookie
            this.redirect('overview', true, false, false)
        },
        /**
         * This function redirects the user to the specified url.
         * @param  {String} url          URL to be routed to
         */
        redirect: function(url) {

            this.$router.replace(url);
        },

        /**
         * 
         * @param  {JSONObject} response The response object sent by the backend
         * @param  {function} onValid  Callback function to be called if the response carries the flag success set to 'true'
         * @param  {function} failure  Callback function to be called if the response carries the flag success set to 'false' and
         *                             other error is not caused by an invalidAccessToken or is an internal database error
         */
        checkServerResponse: function(response, onValid, failure) {
            if (response.data.success === true) {
                onValid(response.data.payload)
            } else if (response.data.success === false && response.data.invalidAccessToken === true) {
                console.log('Invalid access Token, redirecting to login')
                this.redirect('/', false, true, true)
            } else if (response.data.success === false && response.data.errorCode === 10) {
                console.log('An internal database error occured')
            } else {
                console.log('An other error occured...')
                failure(response.data)
            }
        }
    }
}