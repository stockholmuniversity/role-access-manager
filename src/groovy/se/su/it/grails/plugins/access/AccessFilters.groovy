package se.su.it.grails.plugins.access

import grails.util.Environment

class AccessFilters {

  def accessService

  def filters = {

    all(controller: '*', action: '*') {
      before = {

        /** Add development mode. */
        if (Environment.current == Environment.DEVELOPMENT && !(request?.eppn)) {
          return true
        }

        /** Allow access to all unprotected controllers */
        if ((controllerName == null) || accessService.unprotectedControllers.contains(controllerName)) {
          /** Allow access to '/' by default (through controllerName == null). */
          return true
        }

        if (session?.roles == null) {
          //TODO: Get stuff from config
          def entitlements = request.getAttribute("entitlement")?.split(";")

          String scopedEnvironment = accessService.scopedEnvironment

          session?.roles = entitlements?.collect { String entitlement ->
            try {

              String base = AccessRole.getBaseFromUri(entitlement)
              String env = AccessRole.getEnvFromUri(entitlement)

              if (env != scopedEnvironment) {
                log.debug "Environment is $env and scoped environment (set through config is $scopedEnvironment, skipping"
                return null
              }

              return AccessRole.findByBaseAndEnv(base, env)
            } catch (ex) {
              log.error "Failed to parse ${entitlement}", ex
              return null
            }
          }
          session?.roles?.removeAll { it == null }
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