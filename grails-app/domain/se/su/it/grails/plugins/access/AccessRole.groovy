package se.su.it.grails.plugins.access

import groovy.util.logging.Slf4j

@Slf4j
class AccessRole {

  String displayName
  String uri

  public final static String BASE = "urn:mace:swami.se:gmai:"

  static mapping = {
    cache true
    version false // We don't seem to care about optimistic locking anyways.
  }

  static constraints = {
    displayName(nullable: false, blank: false)
    uri(nullable: false, blank: false, unique: true)
  }

  public static AccessRole createOrUpdateInstance(String displayName, String system, String role, Map scope) {

    if (!displayName) {
      throw new IllegalArgumentException("Display name is invalid '$displayName'")
    }

    String uri = composeUri(system, role, scope)

    withTransaction { status ->
      try {
        findOrSaveWhere([displayName: displayName, uri: uri])
      } catch (ex) {
        log.error "Failed to create/update RoleAccess for uri: $uri", ex
        status.setRollbackOnly()
      }
    }
  }

  private static String composeUri(String system, String role, Map scope) {
    if (!system) {
      throw new IllegalArgumentException("System name is invalid '$system'")
    }


    if (!role) {
      throw new IllegalArgumentException("System name is invalid '$role'")
    }

    StringBuilder sb = new StringBuilder()

    sb.append(BASE + system + ":" + role)

    scope?.each { k, v ->
      sb.append(":$k=$v")
    }

    return sb.toString()
  }

  @Override
  int hashCode() {
    int result = 17
    result = 31 * result + uri.hashCode()

    return result
  }

  @Override
  boolean equals(Object o) {
    if (!o instanceof AccessRole) {
      return super.equals(o)
    }

    return (this.uri == (o as AccessRole).uri)
  }

  String toString() {
    return "displayName => $displayName,  uri => $uri"
  }
}
