package se.su.it.grails.plugins.access

class RoleControllerAccess {

  String controller

  static hasMany = [roles: AccessRole]

  static mapping = {
    controller lazy:false
    cache true
  }

  static constraints = {
    roles(nullable:true, blank:true, unique:false)
    controller(unique: true, nullable:false, blank: false)
  }

  String toString() {
    controller
  }
}
