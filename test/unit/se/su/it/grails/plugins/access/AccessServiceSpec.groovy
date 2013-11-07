package se.su.it.grails.plugins.access

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.IgnoreRest
import spock.lang.Specification
import spock.lang.Unroll

@TestFor(AccessService)
@Mock([AccessRole, RoleControllerAccess])
class AccessServiceSpec extends Specification {

  @Unroll
  def "hasAccess: With single role querying for #controllerName expecting #expected"() {
    given:
    AccessRole accessRole = new AccessRole(displayName: 'displayName', uri:'uri').save()
    new RoleControllerAccess(controller: 'controller', roles:[accessRole]).save()

    expect:
    expected == service.hasAccess(accessRole.id, controllerName)

    where:
    controllerName      | expected
    "controller"        | true
    "fooController"     | false
  }

  def "hasAccess: With multiple roles, querying for #controllerName expecting #expected"() {
    given:
    AccessRole accessRole1 = new AccessRole(displayName: 'displayName', uri:'uri').save()
    AccessRole accessRole2 = new AccessRole(displayName: 'displayName', uri:'uri').save()
    new RoleControllerAccess(controller: 'controller', roles:[accessRole2]).save()

    expect:
    service.hasAccess([accessRole1.id, accessRole2.id], "controller")
  }

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

  def "parseUrn: urn without role returns"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]

    expect:
    null == service.parseUrn("urn:mace:swami.se:gmai:signuptool::env=dev:env=prod:dept=501:dept=221:box=1")
  }

  def "getUserRolesIds: with no valid entitlements"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]

    expect:
    null == service.getUserRolesIds("eppn", ["urn:mace:swami.se:gmai:vfu:sysadmin"])
  }

  def "findAuthorizedRoleIds: when supplied parsed urn is empty"() {
    expect:
    null == service.findAuthorizedRoleIds("appName", null)
  }

  def "getUserRolesIds"() {
    given:
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev:env=prod:dept=221:dept=501:box=1").save()
    // shouldn't be found since env does not match.
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=prod:dept=221").save()

    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev").save()

    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin").save()

    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]
    Set expected = [1, 3, 4]

    expect:
    expected == service.getUserRolesIds("eppn", ["urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev"])
  }

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
    service.findAuthorizedRoleIds("signuptool",
        [role:"sysadmin",
            scope:[
              env:["prod"],
              dept:["400"]
            ]
        ]
    ) == [1, 4, 5, 6]
  }

  def "findAuthorizedRoles: with multiple scope attrs"() {
    given:
    service.grailsApplication = [config:[access:[applicationName:"signuptool"]]]
    /* Should find this attr */
    new AccessRole(displayName: "Sysadmin", uri:"urn:mace:swami.se:gmai:signuptool:sysadmin:env=dev").save()

    expect:
    service.findAuthorizedRoleIds("signuptool", [
        role:"sysadmin",
        scope:[env:["prod", "dev"], dept:["400"]]
    ])?.size() == 1
  }
}
