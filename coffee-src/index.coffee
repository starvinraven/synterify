init = ->
  inputValid = $('#url-input')
    .asEventStream('input')
    .map(getUrl)
    .map(isValidUrl)
    .startWith(false)
    .onValue (isValid) ->
      $('#submit-button').prop('disabled', not isValid)

  $('#submit-button')
    .asEventStream('click')
    .map(getUrl)
    .onValue (url) ->
      src = "/doit?url=#{url}"
      li = $('<li>').append($('<img>', {src}), $('<a>', {href: src}).text("Link"))
      $('#images').prepend(li)

getUrl = ->
  $('#url-input').val()

isValidUrl = (url) ->
  url.match(/(https?:\/\/[^\s]+)/)?

init()