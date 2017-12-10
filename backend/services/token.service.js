/**
 * Function to calculate a new expiry date for tokens.
 *
 * @param {Int} validTime  Time in millisecond the token should be valid
 * @param  {Date} startDate  Optional. Use startDate instead of current date to calculate expiry date
 * @return {Date}  new epxiry date
 */
exports.getNewExpiryDate = function(validTime, startDate) {
    let newExpDate;
    if (startDate) {
      newExpDate = startDate.getTime() + validTime;
    } else {
      newExpDate = new Date().getTime() + validTime;
    }
    return new Date(newExpDate);
  };
