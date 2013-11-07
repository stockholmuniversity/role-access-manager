package se.su.it.grails.plugins.access

class AccessTagLib {

  static namespace = "access"

  def grailsApplication
  def accessService

  def hasAccess = { attrs, body ->
    String controller = attrs?.controller ?: null
    if (accessService.hasAccess(session.roleIds as List<Long>, controller as String))
      out << body()
  }

  def renderAccessMatrix = { attrs ->
    String controller = attrs?.controller ?: null
    AccessRole role = AccessRole.get(attrs?.role as long)
    
    def systemRole = RoleControllerAccess.findByController(controller)
    List<String> disabledControllers = grailsApplication.config.access.disabledInDynamicAccess ?: []
    
    if (role && controller) {
      def key = controller
      def val = systemRole?.roles?.contains(role) ?: false
      out << g.checkBox(name:"checkbox_${role}_${key}", value:val, disabled: disabledControllers.contains(controller))
    }
  }
}
