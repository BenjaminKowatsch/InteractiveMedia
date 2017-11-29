<script>
export default {
  name: 'googlesigninbutton' ,
  /**
   * Render an own HTML template by defining this render method.
   * This function creates a template for a div tag with the classes 'loginBtn loginBtn--google',
   * a vue specific ref-attribute set to 'googleSignIn' to uniquely identify the div tag,
   * and enable slots to allow passing callback functions
   * @param  {function} createElement Vue function createElement to create the div tag
   * @return {HTMLObject}               HTML div tag
   */
  render: function(createElement){
    return createElement('div', {
      attrs:{
        class: 'loginBtn loginBtn--google'
      },
      ref: 'googleSignIn'
    }, this.$slots.default);
  },
  /**
   * Register an click event listener to send the login request to google when clicked
   * and call the registered slot function callbacks
   */
  mounted: function(){

    var auth2 = gapi.auth2.getAuthInstance();
      // Set callbacks for successfully signing in and failing during sign in
    auth2.attachClickHandler(this.$refs.googleSignIn, {}, googleUser => {
      // success
      this.$emit('success', googleUser)
    }, error => {
      // error
      this.$emit('error', error)
    });
  }
}
</script>

<style>
/* Google */
.loginBtn--google {
  /*font-family: "Roboto", Roboto, arial, sans-serif;*/
  background: #DD4B39;
}
.loginBtn--google:before {
  border-right: #BB3F30 1px solid;
  background: url('https://s3-us-west-2.amazonaws.com/s.cdpn.io/14082/icon_google.png') 6px 6px no-repeat;
}
.loginBtn--google:hover,
.loginBtn--google:focus {
  background: #E74B37;
}
</style>
