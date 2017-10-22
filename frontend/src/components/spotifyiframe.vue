<script>
export default {
  name: 'spotifyiframe',
  /**
   * Render an own HTML template by defining this render method.
   * This function creates a template for a iframe tag
   * @param  {function} createElement Vue function createElement to create the iframe tag
   * @return {HTMLObject}               HTML iframe tag
   */
  render: function(createElement){
    return createElement('iframe', {
      attrs:{
        width: "300",
        height: "80",
        frameborder: "0",
        allowtransparency: "true"
      }
    });
  },
  /**
   * Resize the iframe to the width of the parent html element when mounted
   */
  mounted: function(){
    var value = this.$el
    var w = value.parentNode.offsetWidth;
    value.style.width = w+"px";
  }
}
</script>

<style>

</style>
