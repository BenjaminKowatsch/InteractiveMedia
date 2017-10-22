let cookie = {}
export default cookie
/**
 * This function returns the content of a cookie with the specified name
 * @param  {String} cname  Name of the cookie to be returned
 * @return {String}       If the cookie exists the content of the cookie is returned.
 *                        Otherwise an empty string '' is returned
 */
cookie.getCookie = function(cname) {
   var name = cname + "=";
   var decodedCookie = decodeURIComponent(document.cookie);
   var ca = decodedCookie.split(';');
   for(var i = 0; i <ca.length; i++) {
       var c = ca[i];
       while (c.charAt(0) == ' ') {
           c = c.substring(1);
       }
       if (c.indexOf(name) == 0) {
           return c.substring(name.length, c.length);
       }
   }
   return "";
}
/**
 * This function create a cookie with the specific name and content and an optional time to live which is set to 60 minutes by default
 * @param  {String} cname    Name of the cookie to access it later
 * @param  {String} content  Content to be stored into the cookie
 * @param  {Number} [ttl=60] Optional, Time to live in minutes
 */
cookie.createCookie = function(cname, content, ttl = 60){
  var now = new Date();
  var time = now.getTime();
  time += ttl*60000;
  now.setTime(time);
  console.log("Creating cookie: "+ cname+ " with content: "+ content+" and an hour lifetime");
  document.cookie = cname+"="+content+"; expires=" + now.toUTCString();
}
/**
 * Sets the expiry date of the specific cookie to a date in the past, so the cookie will be deleted by the browser
 * @param  {String} cname Name of the cookie to be deleted
 */
cookie.deleteCookie = function (cname) {
  console.log("Deleting cookie: "+ cname);
  document.cookie = cname + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';
}
/**
 * Uses the createCookie function to store a JSONObject into a cookie
 * @param  {String} cname       Name of the cookie to be stored
 * @param  {JSONObject} jsonContent JSON Object to be stored
 * @param  {Number} [ttl=60]    Optional, time to live
 */
cookie.createJSONCookie = function(cname, jsonContent, ttl = 60){
  this.createCookie(cname, JSON.stringify(jsonContent),ttl);
}
/**
 * Tries to parse the cookie content into a JSONObject.
 * @param  {String} cname Name of the cookie with the JSON content
 * @return {JSONObject}   If successful, a JSONObject will be returned.
 *                        Otherwise an empty string will be returned.
 */
cookie.getJSONCookie = function(cname){
  var jsonCookie = this.getCookie(cname);
  if("" !== jsonCookie){
    try{
      return JSON.parse(jsonCookie);
    }catch(e){
      return "";
    }
  }
  return "";
}
