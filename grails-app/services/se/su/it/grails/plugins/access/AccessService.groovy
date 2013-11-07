package se.su.it.grails.plugins.access

class AccessService {

  def grailsApplication

  public hasAccess(AccessRole role, String controller) {
    RoleControllerAccess roleControllerAccess = RoleControllerAccess.findByController controller

    roleControllerAccess?.roles?.contains(role) ?: false
  }

  public hasAccess(Collection<AccessRole> roles, String controller) {
    boolean access = false

    roles.each { role ->
      if ( hasAccess(role, controller) )
        access = true
    }

    access
  }

  public addAccess(AccessRole role, String controller) {
    RoleControllerAccess roleControllerAccess = RoleControllerAccess.findOrCreateWhere(controller: controller)

    if (roleControllerAccess && !roleControllerAccess.roles?.contains(role)) {
      roleControllerAccess.addToRoles(role)
      if (!roleControllerAccess.save())
        throw new RuntimeException("Unable to save ControllerAccess ${controller} for role ${role}, failed with error: ${roleControllerAccess.errors?.allErrors?.join(',')}")
    }
  }

  public removeAccess(AccessRole role, String controller) {
    RoleControllerAccess roleControllerAccess = RoleControllerAccess.findByController controller

    if (roleControllerAccess && roleControllerAccess.roles?.contains(role)) {
      roleControllerAccess.removeFromRoles(role)
      if (!roleControllerAccess.save())
        throw new RuntimeException("Unable to remove access for role ${role} to controller ${controller}, failed with error: ${roleControllerAccess.errors?.allErrors?.join(',')}")
    }
  }

  public toggleAccess(AccessRole role, String controller) {
    if (!role) {
      throw new IllegalArgumentException()
    }

    if (!controller) {
      throw new IllegalArgumentException()
    }

    RoleControllerAccess roleControllerAccess = RoleControllerAccess.findOrCreateWhere(controller: controller)

    if (roleControllerAccess.roles?.contains(role)) {
      roleControllerAccess.removeFromRoles(role)
    } else {
      roleControllerAccess.addToRoles(role)
    }

    if(!roleControllerAccess.save()) {
      throw new RuntimeException(
              "Unable to save ControllerAccess ${controller} for role ${role}, failed with error: ${roleControllerAccess?.errors?.allErrors?.join(',')}"
      )
    }
  }

  public List<String> getUnprotectedControllers() {
    grailsApplication.config.access.unprotected ?: []
  }

  public LinkedHashMap<String, String> getRedirect() {
    grailsApplication.config.access.redirect ?: [uri: '/']
  }

  public String getScopedEnvironment() {
    (grailsApplication.config.access.env)?:'dev'
  }

  public Map parseUrn(String urn) {
    Map response = [:]

    if (!urn.contains(AccessRole.BASE)) {
      log.info "urn: $urn does not contain $AccessRole.BASE, skipping."
      return null
    }

    urn = urn - AccessRole.BASE

    /** Split and turn the list around so we can pop! */
    List urnElements = urn.split(":").reverse()


    response.system = urnElements.pop()
    response.role   = urnElements.pop()

    response.scope = createScopeMapFromScope(urnElements)

    return response
  }

  private static Map createScopeMapFromScope(List urn) {
    String[] scope = urn.grep { it.contains("=") }

    Map scopeMap = new TreeMap() // We want a sortedMap.
    for (String scopeEntry in scope) {
      List keyVal = scopeEntry.split("=")
      String key = keyVal[0]
      String val = keyVal[1]

      if (scopeMap.containsKey(key)) {
        scopeMap[key] << val
      } else {
        scopeMap[key] = new TreeSet([val])
      }
    }
    return scopeMap
  }
}
