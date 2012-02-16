<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>
    <title>Controller Audit</title>
    <meta name="layout" content="main"/>
  </head>
  <body>
    <h1>Audit för ${auditedController}</h1>
    <g:link class="whereWasI apps-add-bullet-back">Tillbaka</g:link>
    <br/>
    <br/>
    <g:if test="${audits}">
      <ol>
        <g:each in="${audits}" var="audit">
          <li>${audit}</li>
        </g:each>
      </ol>
    </g:if>
    <g:else>
      Det finns ingen förändringshistorik för kontrollern ${auditedController}.
    </g:else>
  </body>
</html>
