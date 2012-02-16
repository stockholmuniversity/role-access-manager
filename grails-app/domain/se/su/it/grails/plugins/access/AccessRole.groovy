package se.su.it.grails.plugins.access

enum AccessRole {

  SYSADMIN("System Administrator", "urn:mace:swami.se:gmai:su-timeedittool:sysadmin")

  String displayName
  String uri

  AccessRole(String displayName, String uri) {
    this.displayName = displayName
    this.uri = uri
  }

  static AccessRole findByUri(String uri) {
    AccessRole.values().find { accessRole ->
      accessRole.uri == uri
    }
  }
}