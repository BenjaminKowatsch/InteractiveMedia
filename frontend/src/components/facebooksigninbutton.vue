<script>
export default {
  name: 'facebooksigninbutton' ,
  /**
   * Render an own HTML template by defining this render method.
   * This function creates a template for a div tag with the classes 'loginBtn loginBtn--facebook',
   * a vue specific ref-attribute set to 'facebookSignIn' to uniquely identify the div tag,
   * and enable slots to allow passing callback functions
   * @param  {function} createElement Vue function createElement to create the div tag
   * @return {HTMLObject}               HTML div tag
   */
  render: function(createElement){
    return createElement('div', {
      attrs:{
        class: 'loginBtn loginBtn--facebook'
      },
      ref: 'facebookSignIn'
    }, this.$slots.default);
  },
  /**
   * Define a property to be passed when using the template.
   * This property shall contain the facebook specific data object required for loggin the user in
   * @type {Object}
   */
  props: {
    params:{
      type: Object,
      required: true,
      default () {
        return {}
      }
    }
  },
  /**
   * Register an click event listener to send the login request to facebook when clicked
   * and call the registered slot function callbacks
   */
  mounted: function(){
    this.$refs.facebookSignIn.addEventListener('click', e => {
      // Login with Facebook on click
      window.FB.login(response => {
        this.$emit(response.authResponse ? 'success' : 'error', response)
      }, this.params);
    });
  }
}
</script>

<style>

/* Facebook */
.loginBtn--facebook {
  background-color: #4C69BA;
  background-image: linear-gradient(#4C69BA, #3B55A0);
  /* font-family: "Helvetica neue", Helvetica Neue, Helvetica, Arial, sans-serif;*/
  text-shadow: 0 -1px 0 #354C8C;
}
.loginBtn--facebook:before {
  border-right: #364e92 1px solid;
  background: url('https://s3-us-west-2.amazonaws.com/s.cdpn.io/14082/icon_facebook.png') 6px 6px no-repeat;
}
.loginBtn--facebook:hover,
.loginBtn--facebook:focus {
  background-color: #5B7BD5;
  background-image: linear-gradient(#5B7BD5, #4864B1);
}
</style>
