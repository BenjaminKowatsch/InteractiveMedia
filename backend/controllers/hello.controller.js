exports.getHello = function(req, res) {
  res.jsonp({'status': 'up', 'currentDate': new Date()});
};
