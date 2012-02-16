package se.su.it.grails.plugins.access

class AccessFilters {

  def accessService

  def filters = {

    all(controller: '*', action: '*') {
      before = {

        if(session?.roles == null) {

          //TODO: Get stuff from config
          def entitlements = request.getAttribute("entitlement")?.split(";")

          session?.roles = entitlements?.collect { entitlement ->
            AccessRole.findByUri(entitlement)
          }

          session?.roles?.removeAll { it == null }
        }

        if (accessService.unprotectedControllers.contains(controllerName))
          return true

        boolean hasAccess = false
        session?.roles?.each { role ->
          if (accessService.hasAccess(role, controllerName))
            hasAccess = true
        }

        if ( !hasAccess ) {
          //TODO: i18n
          flash.error = "Access denied for user ${session.user} with roles ${(session.roles.collect { it.displayName }.join(', '))} to $controllerName"
          redirect(accessService.redirect)
          return false
        }
        return true
      }
    }
  }
}