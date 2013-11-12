package se.su.it.grails.plugins.access

class RoleControllerAccess {

  String controller

  static hasMany = [roles: AccessRole]

  static mapping = {
    cache true
    version false // We don't seem to care about optimistic locking anyways.
    roles batchSize: 10
  }

  static constraints = {
    roles(nullable:true)
    controller(nullable:false, blank: false, unique: true)
  }

  static namedQueries = {
    accessByControllerAndRoleId { String controllerName, Long roleId ->
      eq 'controller', controllerName
      roles {
        eq 'id', roleId
      }
    }
  }

  String toString() {
    controller
  }
}
