package se.su.it.grails.plugins.access

class AccessTagLib {

  static namespace = "access"

  def grailsApplication
  def accessService

  def hasAccess = { attrs, body ->
    String controller = attrs?.controller ?: null
    def roles = session.roles

    if (accessService.hasAccess(roles, controller))
      out << body()
  }

  def renderAccessMatrix = { attrs ->
    String controller = attrs?.controller ?: null
    AccessRole role = AccessRole.findById attrs?.role
    
    def systemRole = RoleControllerAccess.findByController(controller)
    List<String> disabledControllers = grailsApplication.config.access.disabledInDynamicAccess ?: []
    
    
    if (role && controller) {
      def key = controller
      def val = systemRole?.roles?.contains(role) ?: false
      out << g.checkBox(name:"checkbox_${role}_${key}", value:val, disabled: disabledControllers.contains(controller))
    }
  }
}
