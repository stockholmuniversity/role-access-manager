package se.su.it.grails.plugins.access

class RoleControllerAccess {

  String controller

  static hasMany = [roles: AccessRole]

  static mapping = {
    cache true
    roles batchSize: 10
  }

  static constraints = {
    roles(nullable:true)
    controller(nullable:false, blank: false, unique: true)
  }

  String toString() {
    controller
  }
}
