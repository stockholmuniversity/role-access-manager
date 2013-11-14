package se.su.it.grails.plugins.access

import groovy.util.logging.Slf4j

@Slf4j
class AccessFilters {

  def grailsApplication
  def accessService

  def filters = {

    all(controller: '*', action: '*') {
      before = {

        /** If plugin is disabled, allow shibboleth bypass. */
        if (grailsApplication?.config?.access?.disabled == true) {
          log.info "Access disabled (access.disabled = true), allowing anything to pass."
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
