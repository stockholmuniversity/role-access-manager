package se.su.it.grails.plugins.access

import grails.util.Environment
import groovy.util.logging.Slf4j

@Slf4j
class AccessFilters {

  def grailsApplication
  def accessService

  def filters = {

    all(controller: '*', action: '*') {
      before = {

        /** Add development & mock mode shibboleth bypass. */
        if (
            Environment.current.name in [Environment.DEVELOPMENT.name, "mock"]
                && !(request?.eppn)) {
          log.info "Missing eppn but environment $Environment.current.name allows shibboleth bypass, returning true."
          return true
        }

        /** Allow access to all unprotected controllers */
        if ((controllerName == null) || accessService.unprotectedControllers.contains(controllerName)) {
          /** Allow access to '/' by default (through controllerName == null). */
          return true
        }

        String eppn = request.eppn
        Set roleIds = session.roleIds

        if (!roleIds) {
          List entitlements = request.getAttribute("entitlement")?.split(";")
          roleIds = accessService.getUserRolesIds(eppn, entitlements)
          session.roleIds = roleIds
        }

        boolean hasAccess = accessService.hasAccess(roleIds, controllerName)

        if (!hasAccess) {
          flash.error = "Access denied for user ${eppn}"
          redirect(accessService.redirect)
          return false
        }
        return true
      }
    }
  }
}
