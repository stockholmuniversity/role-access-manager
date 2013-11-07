package se.su.it.grails.plugins.access

import grails.test.mixin.TestFor
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AccessService)
class AccessServiceSpec extends Specification {

  def "parseUrn: invalid urn"() {
    expect:
    null == service.parseUrn("urn:mace:swami.se:gmai:signuptool:sysadmin".replace("swami.se", "kaka.se"))
  }

  @Unroll
  def "parseUrn: Parsing urn #urn"() {
    expect:
    result == service.parseUrn(urn)

    where:
    urn                                                                                   | result
    "urn:mace:swami.se:gmai:signuptool:sysadmin"                                          |
        [system:'signuptool', role:'sysadmin', scope:[:]]
    "urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev"                                  |
        [system:'signuptool', role:'sysadmin', scope:[env:new TreeSet(["dev"])]]
    "urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod"                         |
        [system:'signuptool', role:'sysadmin', scope:[env:new TreeSet(["dev", "prod"])]]
    "urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=221:box=1" |
        [system:"signuptool", role:"sysadmin", scope:[box:new TreeSet(["1"]), dept:new TreeSet(["221", "501"]), env:new TreeSet(["dev", "prod"])]]
  }

  def "createScopeMapFromScopeList"() {
    given:
    Map result = new TreeMap([box:new TreeSet(["1"]), dept:new TreeSet(["221", "501"]), env:new TreeSet(["dev", "prod"])])

    expect:
    result == service.createScopeMapFromScope("urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=221:box=1".split(":") as List)
  }
}
