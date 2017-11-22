exports.getHello = function(req, res) {
  res.jsonp({'msg': new Date()});
};
