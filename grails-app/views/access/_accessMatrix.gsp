<g:render template="javascripts/accessMatrix"/>
<table>
  <thead>
    <tr>
      <th style="width:100px;"></th>
      <g:each in="${roles}" var="role">
        <th>${role.displayName}</th>
      </g:each>
    </tr>
  </thead>
  <tbody>
    <g:each in="${grailsApplication.controllerClasses?.sort { it.shortName }}">
      <g:set var="cntrl" value="${it}"/>
      <tr>
        <td style="padding-top:3px; padding-bottom:3px;">
          ${cntrl?.logicalPropertyName}
        </td>
        <g:each in="${roles}" var="role">
          <td data-role="${role}" data-cntrl="${cntrl?.logicalPropertyName}" style="padding-top:3px; padding-bottom:3px;">
            <access:renderAccessMatrix role="${role}" controller="${cntrl?.logicalPropertyName}"/>
          </td>
        </g:each>
      </tr>
    </g:each>
  </tbody>
</table>
