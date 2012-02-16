package se.su.it.grails.plugins.access

class AccessTagLib {

  static namespace = "access"

  def accessService

  def hasAccess = { attrs, body ->
    String controller = attrs?.controller ?: null
    def roles = session.roles

    if (accessService.hasAccess(roles, controller))
      out << body()
  }
  
  def renderAccessMatrix = { attrs ->
    String controller = attrs?.controller ?: null
    AccessRole role = attrs?.role
    def systemRole = RoleControllerAccess.findByController(controller)

    if (role && controller) {
      def key = controller
      def val = systemRole?.roles?.contains(role) ?: false
      out << g.checkBox(name:"checkbox_${role}_${key}", value:val)
    }
  }
}
