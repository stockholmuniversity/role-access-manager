import se.su.it.grails.plugins.access.AccessFilters
import org.codehaus.groovy.grails.plugins.web.filters.FiltersConfigArtefactHandler

class RoleAccessManagerGrailsPlugin {
  // the plugin version
  def version = "0.2.7"
  def groupId = "se.su.it.grails.plugins"
  // the version or versions of Grails the plugin is designed for
  def grailsVersion = "2.0 > *"
  // the other plugins this plugin depends on
  def dependsOn = [:]

  def loadBefore = ['filters']

  // resources that are excluded from plugin packaging
  def pluginExcludes = [
          "grails-app/views/error.gsp"
  ]

  def title = "Role Access Manager Plugin" // Headline display name of the plugin
  def author = "Joakim Lundin"
  def authorEmail = "joakim.lundin@su.se"
  def description = '''\
Grails plugin to manage role access.
'''

  // URL to the plugin's documentation
  def documentation = ""

  // Extra (optional) plugin metadata

  // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

  // Details of company behind the plugin (if there is one)
  def organization = [ name: "Stockholm University", url: "http://www.su.se/" ]

  // Any additional developers beyond the author specified above.
  def developers = [
          [ name: "Tommy Andersson", email: "tommy.andersson@su.se" ],
          [ name: "Jan Qvarnström", email: "jan.qvarnstrom@su.se"]
  ]

  // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

  // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.grails-plugins.codehaus.org/browse/grails-plugins/" ]

  def doWithWebDescriptor = { xml ->
  }

  def doWithSpring = {
    application.addArtefact(FiltersConfigArtefactHandler.TYPE, AccessFilters.class)
  }

  def doWithDynamicMethods = { ctx ->
    // TODO Implement registering dynamic methods to classes (optional)
  }

  def doWithApplicationContext = { applicationContext ->
  }

  def onChange = { event ->
    // TODO Implement code that is executed when any artefact that this plugin is
    // watching is modified and reloaded. The event contains: event.source,
    // event.application, event.manager, event.ctx, and event.plugin.
  }

  def onConfigChange = { event ->
    // TODO Implement code that is executed when the project configuration changes.
    // The event is the same as for 'onChange'.
  }

  def onShutdown = { event ->
    // TODO Implement code that is executed when the application shuts down (optional)
  }
}

