<table>
  <thead>
  <tr>
    <th style="width:100px;"></th>
    <g:each in="${roles}" var="role">
      <th data-urn="${role.uri}" class="displayName">${role.displayName}</th>
    </g:each>
  </tr>
  <tr id="roleInfo"><th colspan="${roles?.size() + 1}" style="height: 25px;"></th></tr>
  </thead>
  <tbody>
  <g:each in="${grailsApplication.controllerClasses?.sort { it.shortName }}">
    <g:set var="cntrl" value="${it}"/>
    <tr>
      <td style="padding-top:3px; padding-bottom:3px;">
        ${cntrl?.logicalPropertyName}
      </td>
      <g:each in="${roles}" var="role">
        <td data-roleName="${role?.displayName}" data-role="${role?.id}" data-cntrl="${cntrl?.logicalPropertyName}"
            style="padding-top:3px; padding-bottom:3px;">
          <access:renderAccessMatrix role="${role.id}" controller="${cntrl?.logicalPropertyName}"/>
        </td>
      </g:each>
    </tr>
  </g:each>
  </tbody>
</table>
