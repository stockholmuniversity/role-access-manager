package se.su.it.grails.plugins.access

class AccessController {

  def accessService

  def index = {
    [roles:AccessRole.values()]
  }

  def toggleAccess = {
    AccessRole role = params?.role
    def targetController = params?.cntrl

    if (!role) {
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.roleNotFound', args:[params?.role])}")
    }

    if (!targetController) {
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.controllerNotFound', args:[params?.cntrl])}")
    }

    try {
      accessService.toggleAccess(role, targetController)
    } catch (ex) {
      log.error ex
      return response.sendError(400,"${g.message(code:'controllers.access.toggleAccess.actionFailed')}")
    }
    return render(text:"${g.message(code:'generic.update.successful')}")
  }
}
