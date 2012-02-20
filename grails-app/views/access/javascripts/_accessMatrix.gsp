<script type="text/javascript">
  $(function() {
    //$('input[name$=_section],input[name$=_access],input[name$=_value],input[name$=_dashboard],input[name$=_error],input[name$=_selenium]').attr('disabled', true);

    $(':checkbox').change(function(event) {
      event.preventDefault();
      var checkBox = $(this);
      var updatedEntry = $(this).closest('td');
      var role = updatedEntry.data('role');
      var cntrl = updatedEntry.data('cntrl');

      $.ajax({
        url:"${g.createLink(controller:'access', action:'toggleAccess')}",
        type:"POST",
        data:{role:role, cntrl:cntrl},
        success: function(data) {
          $('.feedbackMsgs').html(wrapMessage("${g.message(code:'controllers.access.toggleAccess.actionSuccessful')}", 2000, 0))
        },
        error: function(data) {
          $('.feedbackMsgs').html(wrapError(data.responseText));
          rollbackCheckBoxStatus(checkBox)
        }
      })
    })
  });
</script>
