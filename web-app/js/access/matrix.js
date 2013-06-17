$(function() {

  var feedback$ = $('div.feedbackMsgs');

  function clickHandler( checkBox$ ) {
    feedback$.html('');

    var updatedEntry = checkBox$.closest('td');
    var role = updatedEntry.data('role');
    var roleName = updatedEntry.data('rolename');
    var cntrl = updatedEntry.data('cntrl');

    $.ajax({
      url:"/access/toggleAccess",
      type:"POST",
      data:{role:role, cntrl:cntrl},
      success: function(data) {
        feedback$.html(wrapMessage("Role " + roleName + " access successfully updated for controller " + cntrl, 2000, 0)) },
      error: function(data) {
        alert(data.responseText);
      }
    })
  }

  $(':checkbox').change(function(event) {
    event.preventDefault();
    clickHandler( $(this) );
  });

  function wrapMessage(message, delay, fadeTime) {
    if (delay == undefined) {
      delay = 2000
    }
    if (fadeTime == undefined) {
      fadeTime = 1500
    }
    var element = "<div class='apps-dialogue-green-small'>" + message + "</div>";

    if (delay > 0 && fadeTime > 0) {
      return $(element).show().delay(delay).fadeOut(fadeTime);
    } else {
      return $(element).show()
    }
  }

});
