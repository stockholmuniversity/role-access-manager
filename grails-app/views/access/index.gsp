<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title><g:message code="views.access.index.title"/></title>
    <meta name="layout" content="main"/>
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.1/jquery.min.js"></script>
    <script src="${g.resource(dir:'js/access', file:'matrix.js')}"></script>
  </head>

  <body>
    <h1><g:message code="views.access.index.title"/></h1>
    <g:link controller="dashboard" action="index" class="apps-add-bullet-back"><g:message code="generic.back"/></g:link>
    <div class="feedbackMsgs"></div>
    <div class="apps-float-80">
      <div class="apps-content-block-inner">
        <div id="roleAccess">
          <g:render template="accessMatrix" bean="accessInstance"/>
        </div>
      </div>
      <div class="clear-float"></div>
    </div>
  </body>
</html>
