(function() {
  var getUrl, init, isValidUrl;

  init = function() {
    var inputValid;
    inputValid = $('#url-input').asEventStream('input').map(getUrl).map(isValidUrl).startWith(false).onValue(function(isValid) {
      return $('#submit-button').prop('disabled', !isValid);
    });
    return $('#submit-button').asEventStream('click').map(getUrl).onValue(function(url) {
      var li, src;
      src = "/doit?url=" + url;
      li = $('<li>').append($('<img>', {
        src: src
      }), $('<a>', {
        href: src
      }).text("Link"));
      return $('#images').prepend(li);
    });
  };

  getUrl = function() {
    return $('#url-input').val();
  };

  isValidUrl = function(url) {
    return url.match(/(https?:\/\/[^\s]+)/) != null;
  };

  init();

}).call(this);
