package se.su.it.grails.plugins.access

import spock.lang.Specification
import spock.lang.Unroll

class AccessRoleSpec extends Specification {

  def setup() {}
  def cleanup() {}

  @Unroll
  def "composeUri: role: #role + scope: #scope => #output"() {
    expect:
    output == new AccessRole().composeUri(system, role, scope)

    where:
    system  | role    | scope                     | output
    'sys'   | 'foo'   | [:]                       | "urn:mace:swami.se:gmai:sys:foo"
    'sys'   | 'roo'   | [bar:"kaka"]              | "urn:mace:swami.se:gmai:sys:roo:bar=kaka"
    'sys'   | 'goo'   | [bar:"kaka", kaka:"bar"]  | "urn:mace:swami.se:gmai:sys:goo:bar=kaka:kaka=bar"
  }

  def "composeUri: missing system"() {
    when:
    new AccessRole().composeUri(null, "role", [:])

    then:
    thrown(IllegalArgumentException)
  }

  def "composeUri: missing role"() {
    when:
    new AccessRole().composeUri("sys", null, [:])

    then:
    thrown(IllegalArgumentException)
  }

}
