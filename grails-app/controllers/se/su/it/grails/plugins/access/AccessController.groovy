package se.su.it.grails.plugins.access

class AccessController {

  def accessService

  def index = {
    return render(view:'index', model:[roles:AccessRole.list()])
  }

  def toggleAccess = {
    AccessRole role = AccessRole.findById(params.long("role"))
    String targetController = params?.cntrl

    if (!role)
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.roleNotFound', args:[params?.role])}")

    if (!targetController)
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.controllerNotFound', args:[params?.cntrl])}")

    if (grailsApplication.config.access.disabledInDynamicAccess?.contains(targetController))
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.controllerNotAllowedForDynamic', args:[params?.cntrl])}")

    try {
      accessService.toggleAccess(role, targetController)
    } catch (ex) {
      log.error ex
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.actionFailed')}")
    }
    return render(text:"${g.message(code:'generic.update.successful')}")
  }
}
