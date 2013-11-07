package se.su.it.grails.plugins.access

class AccessService {

  def grailsApplication

  public boolean hasAccess(Long id, String controller) {
    hasAccess([id], controller)
  }

  public boolean hasAccess(Collection<? extends Number> ids, String myController) {
    boolean hasAccess = false

    List myRoles = AccessRole.findAllByIdInList(ids?.toList())

    for (role in myRoles) {
      if (RoleControllerAccess.where {
        controller == myController && roles.contains(role)
      }.count()) {
        hasAccess = true
        break
      }
    }

    return hasAccess
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

  private Map parseUrn(String urn) {
    Map response = [:]

    final String appName = grailsApplication.config.access.applicationName

    if (!urn.contains(AccessRole.BASE)) {
      log.info "urn: $urn does not contain $AccessRole.BASE, skipping."
      return null
    }

    urn = urn - AccessRole.BASE

    /** Split and turn the list around so we can pop! */
    List urnElements = urn?.split(":")?.reverse()


    String system = urnElements?.pop()

    if (system != appName) {
      /** Not an urn directed for this system */
      log.error "urn: $urn is not directed at the current system with name $appName (config > access > env)"
      return null
    }

    response.role = urnElements?.pop()

    if (!response.role) {
      /** urn is missing role. */
      log.error "urn: $urn is missing role, skipping."
      return null
    }

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

  public Set getUserRolesIds(String eppn, List<String> entitlements) {

    final String appName = grailsApplication.config.access.applicationName

    log.debug "${eppn} has the following entitlements."
    entitlements.eachWithIndex { entitlement, index ->
      log.debug "${index}. \t $entitlement"
    }

    /** Remove all entitlements that do not correspond to the current application scope. */
    entitlements = entitlements.grep { String entitlement ->
      entitlement?.contains("${AccessRole.BASE + appName}:")
    }

    log.debug "${eppn} will use the following entitlements."
    entitlements.eachWithIndex { entitlement, index ->
      log.debug "${index}. \t $entitlement"
    }

    if (!entitlements) {
      log.error "No valid entitlements found found for $eppn."
      return null
    }

    Set userRoleIds = []

    for (entitlement in entitlements) {
      Map parsedUrn = parseUrn(entitlement)

      if (parsedUrn != null) {
        userRoleIds << findAuthorizedRoleIds(appName, parsedUrn)
      }
    }

    return userRoleIds?.flatten()
  }

  private List<Long> findAuthorizedRoleIds(String appName, Map parsedUrn) {

    if (!parsedUrn) {
      return null
    }

    List roles = []

    List<AccessRole> systemRoles = AccessRole.findAllByUriLike("${AccessRole.BASE + appName + ":" + parsedUrn.role}%")

    for (systemRole in systemRoles) {

      Map parsedSystemRole = parseUrn(systemRole.uri)

      if (!parsedSystemRole) {
        /* Skipping null roles */
        continue
      }

      boolean hasRole = true

      for (String scopeKey in parsedSystemRole.scope.keySet()) {
        if (!parsedUrn?.scope?.containsKey(scopeKey)) {
          /* No matching key in the parsed urn means the restriction does not apply to the current user. */
          continue
        }

        /* If there is no correlation disjoint returns true, then we don't have the specified role. */
        if (Collections.disjoint((TreeSet) parsedUrn.scope[scopeKey], (TreeSet) parsedSystemRole.scope[scopeKey])) {
          hasRole = false
          break
        }
      }

      if (hasRole) {
        roles << systemRole.id
      }
    }

    return roles
  }
}
