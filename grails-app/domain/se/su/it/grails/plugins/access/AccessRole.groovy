package se.su.it.grails.plugins.access

import groovy.util.logging.Slf4j

@Slf4j
class AccessRole {

  String base
  String displayName
  String env
  String uri

  static mapping = {
    cache true
    version false // We don't seem to care about optimistic locking anyways.
  }

  static constraints = {
    base(nullable: false, blank: false, unique:'env')
    displayName(nullable: false, blank: false)
    env(inList: ['dev', 'prod', 'test'], blank: false)
    uri(nullable: false, blank: false, unique: true)
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
      return super.equals()
    }

    return (this.uri == (o as AccessRole).uri)
  }

  public static String getBaseFromUri(String uri) {
    String base = null

    if (!uri) { return null }

    try {
      /** TODO: Sadly implemented regex, should find a better solution, but this will do for now. */
      def matcher = (uri =~ /(urn:mace:swami.se:gmai):(.*?):(.*?)(:.*?)?$/)
        if (matcher?.size() != 1) {
          log.error "Unable to extract base from gmai => $uri"
        } else {
          base = matcher[0][1,2,3].join(':')
        }
    } catch (ex) {
      log.error "Failed to parse base from uri: $uri", ex
    }

    return base
  }

  public static String getEnvFromUri(String uri) {
    String env = null

    if (!uri) { return null }

    try {
      def matcher = (uri =~ /env=(\w+)/)
      if (matcher?.size() != 1) {
        log.error "Unable to extract environment from gmai => $uri"
      } else {
        env = matcher[0][1]
      }
    } catch (ex) {
      log.error "Failed to parse env from uri: $uri", ex
    }

    return env
  }

  public static AccessRole createOrUpdateInstance(String displayName, String uri) {
    AccessRole accessRole = null

    String base = getBaseFromUri(uri)
    String env = getEnvFromUri(uri)

    accessRole = findByBaseAndEnv(base, env)

    withTransaction { status ->
      try {
        if (accessRole) {
          accessRole.displayName = displayName
          accessRole.uri = uri
        } else {
          accessRole = new AccessRole(displayName: displayName, uri: uri)
        }
        accessRole.save(flush:true, failOnError: true)
      } catch (ex) {
        log.error "Failed to create/update RoleAccess for uri: $uri", ex
        status.setRollbackOnly()
      }
    }

  }

  transient beforeValidate() {
    this.base = getBaseFromUri(this.uri)
    this.env = getEnvFromUri(this.uri)
  }

  String toString() {
    return "displayName => $displayName,  uri => $uri,  env => $env"
  }
}