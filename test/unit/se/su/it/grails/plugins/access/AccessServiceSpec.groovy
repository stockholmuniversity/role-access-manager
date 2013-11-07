package se.su.it.grails.plugins.access

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AccessService)
@Mock([AccessRole])
class AccessServiceSpec extends Specification {

  def "parseUrn: invalid urn"() {
    expect:
    null == service.parseUrn("urn:mace:swami.se:gmai:signuptool:sysadmin".replace("swami.se", "kaka.se"))
  }

  @Unroll
  def "parseUrn: Parsing urn #urn"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]

    expect:
    result == service.parseUrn(urn)

    where:
    urn                                                                                   | result
    "urn:mace:swami.se:gmai:vfu:sysadmin"                                                 | null
    "urn:mace:swami.se:gmai:signuptool:sysadmin"                                          |
        [role:'sysadmin', scope:[:]]
    "urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev"                                  |
        [role:'sysadmin', scope:[env:new TreeSet(["dev"])]]
    "urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod"                         |
        [role:'sysadmin', scope:[env:new TreeSet(["dev", "prod"])]]
    "urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=221:box=1" |
        [role:"sysadmin", scope:[box:new TreeSet(["1"]), dept:new TreeSet(["221", "501"]), env:new TreeSet(["dev", "prod"])]]
  }

  def "createScopeMapFromScopeList"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]
    Map result = new TreeMap([box:new TreeSet(["1"]), dept:new TreeSet(["221", "501"]), env:new TreeSet(["dev", "prod"])])

    expect:
    result == service.createScopeMapFromScope("urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=221:box=1".split(":") as List)
  }

  def "getRoles"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]
    def expected = [[role:"sysadmin", scope:[box:new TreeSet(["1"]), dept:new TreeSet(["221", "501"]), env:new TreeSet(["dev", "prod"])]]]

    expect:
    expected == service.getRoles("eppn", ["urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=221:box=1"])
  }
  @IgnoreRest
  def "findAuthorizedRoles"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]
    // Unscoped sysadmin
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin").save()
    // wrong env
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev").save()
    // wrong dept
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=prod:dept=501").save()
    // holds correct env
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod").save()
    // holds correct env and dept
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=400").save()
    // holds correct env and dept
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=501:dept=400:box=4").save()

    expect:
    service.findAuthorizedRoles("signuptool",
        [role:"sysadmin",
            scope:[
              env:["prod"],
              dept:["400"]
            ]
        ]
    ) == [1, 4, 5, 6]
  }
}
