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

        if (!session.roles) {
          String eppn = request.eppn
          List entitlements = request.getAttribute("entitlement")?.split(";")
          session.roles = accessService.getRoles(eppn, entitlements)
        }

        boolean hasAccess = false

        session?.roles?.each { role ->
          if (accessService.hasAccess(role, controllerName)) {
            hasAccess = true
          }
        }

        if (!hasAccess) {
          //TODO: i18n
          String msg = ""
          if (session.roles?.size > 0) {
            msg = "Access denied for user ${request?.eppn} with roles ${(session.roles.collect { it.displayName }.join(', '))} to $controllerName"
          } else {
            msg = "User ${request?.eppn} is lacking valid roles, current environment scope is $accessService.scopedEnvironment"
          }
          flash.error = msg
          redirect(accessService.redirect)
          return false
        }
        return true
      }
    }
  }
}