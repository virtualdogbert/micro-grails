micro-grails
============
This library for [Micronaut](http://micronaut.io/) brings some of the convention over configuration Grails has to Micronaut projects.

## Installation
Use the gradle plugin, rather than using the library directly:\
[Micro Grails plugin](https://github.com/virtualdogbert/micro-grails-plugin/)

## Sample app
To see the library in use check out this sample app:

[test-ast-micro](https://github.com/virtualdogbert/test-ast-micro)

## Usage
### Config
After installing the library via the Gradle plugin, you will need to add a conventions file:
```groovy
conventions = [
        rootPath             : 'micronaut-app',

        controllerPath       : 'controllers',
        commandPath          : 'command',
        domainPath           : 'domain',
        servicePath          : 'services',

        urlMappings          : 'UrlMappings',

        compileStatic        : true,
        compileStaticExcludes: [],

        transactional        : true,
        transactionalExcludes: [],

        debug                : false
]
```
 This sets up the conventions that the library will use:
 * rootPath - think of this as your grails-app directory, although in the example I made it micronaut-app.
 * controllerPath - This will be the directory under the root path, where you will put your controllers.
 * commandPath - For Milestone 2 this will be where you put command objects used for binding in controller classes.
 * domainPath - This will be the folder under the root path, where you will put your domain classes.
 * servicePath - this will be the folder under the root path, where you will put your service classes.
 
 
 * urlMappings - this is the name of the file for the urlMappings, which will be under the controllerPath.
 
 
 * compileStatic - This is a flag to statically compile the groovy classes by default. This will not effect any classes that already have 
 @CompileStatic, or @CompileDynamic. This does not apply to Domain classes in Milestone 1, as I haven't figured out how to properly port the
 compile static extension for mapping.
 * compileStaticExcludes - List of class names as strings, where if the compile static flag is set to true, you can use this to exclude 
 classes, from being statically compiled. All names must be fully qualified with the package name.
 
 
 * transactional - This flag adds @Transactional by default to service classes. This will not effect classes that already have @Transactional
 or @NotTransactional
 * transactionalExcludes - List of service class names as strings, where if the transactional flag is set true, these classes won't have
 @Transactional added to them. All names must be fully qualified with the package name.
 
 * debug - This flag allows you to see the code that is added by convention if the Groovy console, by suspending the directory requirement of
 the convention. See the debug section for more info.
 
 ### UrlMapping
 The UrlMapping, is very simple based of the configure slurper syntax, so it's not a full DSl, like the UrlMapping in Grails.
 
 An example of the URL mapping:
 ```groovy
 home {
     url = '/'
     index {
         url = '/'
         method = 'GET'
         produces = 'text/html'
     }
 }
 
 loginAuth {
     url = '/login'
 
     auth {
         method = 'GET'
         produces = 'text/html'
     }
 
     authFailed {
         method = 'GET'
         produces = 'text/html'
     }
 }
 ```
 * home and loginAuth are controllers.
 * the url is applied to the controller in the annotation @Controller(url)
 * index, auth, authFailed are actions in the controllers and they would get the annotation based on the method, with the action at the url.
 * produces sets the return type of the action which is MediaType.APPLICATION_JSON, by default. 
 
 ### Domain classes
 Domain classes Should be put under the domainPath, and their name should end with 'Domain'. The name ending in 'Domain', is for consistency 
 across the artefacts, and to facilitate the ability to debug, the code that gets added at compile time.
 
 The library will automatically add the @Entity annotation to the class.
 
 ### Controller classes
 Controller classes should be under the controllerPath, and their name should end with 'Controller'
 
 The library will automatically add the @Controller annotation, with the url from the UrlMapping, and the appropriate method annotation(@GET, @POST, etc)
 to each public action, with the name of the action in the annotation. Also from the UrlMapping the library will add a @Produces annotation,
 with it's text set to the produces in the urlMapping.
 
 The library will also go through all of the properties/fields, looking at there types for bean injection annotations(Singleton Context, 
 Prototype ThreadLocal), and will create a constructor for those properties/fields, unless you create a constructor to override. So you get 
 injection by type for free.
 
 Finally the library will add the  @CompileStatic annotation to the controller class based on the flag, and exclusions from the conventions
 config file.  The library will also skip adding @CompileStatic, if the class already has the annotation or the @CompileDynamic annotation.
 
 ### Service classes
 Service classes should be under the servicePath, and their name should end with 'Service'

The library will add the @Singleton annotation automatically to the service, unless it already has another bean annotation(Singleton Context, 
Prototype ThreadLocal).

The library will also go through all of the properties/fields, looking at there types for bean injection annotations(Singleton Context, 
Prototype ThreadLocal), and will create a constructor for those properties/fields, unless you create a constructor to override. So you get 
injection by type for free.

The library will add the  @Transactional annotation to the controller class based on the flag, and exclusions from the conventions
config file.  The library will also skip adding @Transactional, if the class already has the annotation or the @NotTransactional annotation.
 
Finally the library will add the  @CompileStatic annotation to the service class based on the flag, and exclusions from the conventions
config file. The library will also skip adding @CompileStatic, if the class already has the annotation or the @CompileDynamic annotation.
 
 ### Debugging

If you use this library from the plugin project, as intended, you will be able to run `gradle console`, and the gradle console will popup
with the context of the application. If you have the debug flag enabled, then the library will ignore the directory restriction of the
convention of the artefacts, and just use the class name. This will allow you to copy and paste any artefact(controller, service, etc) to 
the console, and open up the AST browser`Ctrl + t`, and then be able to see the code that the library adds to your classes. Debug should 
only be used to see the code that the library add in the console, you should not have it on for general use.

## About this project
This is a side project of mine just to play around with adding similar conventions over configuration, like Grails to Micronaut, using my 
Micro Grails library. This may be completely supplanted by the fact that Grails 4 will use Micronaut for its main context. However because this
is lighter weight, it may appeal to some, and also demonstrates how Grails works with simpler/more constrained code.

## History:
### Milestone 1
* Setup conventions file to be read in as configuration.
  * Setup urlMappings file to be read in as configuration, a simpler stand-in for now as compared to the Grails DSL.
  * Add MicronautCompileStatic, based on GrailsCompileStatic, with it's extensions.
  * Set up Grails conventions Global AST transform delegating to artefact handlers(Domain, Controller, Service).
      * Domain
        * Add Entity annotation.
      * Controller
         * Add Controller annotation with mapping from urlMappings.
         * Add method annotations(GET,PUT, etc) annotations with values from urlMappings.
         * Create injection constructor from properties/fields, that are Services.
         * Add CompileStatic annotation based on conventions configuration.
      * Service
         * Add Singleton annotation by default.
         * Add CompileStatic annotation based on conventions configuration.
         * Add Transactional annotation based on conventions configuration.
* Add source directories using Gradle.
* Add console task using Gradle.
* Add Gradle Task for default conventions setup
* Add ReadME documentation.

## Ideas for next Release
This project is open for pull requests. here are some ideas for the next milestone release. I would like to keep the code as simple as possible,
but no simpler.

* Milestone 2
  * Add more defaults and error checking
  * Add GormEntity Trait to Domain artefacts
     * http://gorm.grails.org/latest/api//org/grails/datastore/gorm/GormEntityApi.html
  * Write GDSL for GormEntity or investigate making an Intellij plugin, because of the limitation is GDSL.
  * Implement Validateable trait for validating command objects.
     * https://github.com/grails/grails-data-mapping/blob/master/grails-datastore-gorm-validation/src/main/groovy/grails/gorm/validation/DefaultConstrainedProperty.groovy
     * https://github.com/grails/grails-data-mapping/blob/master/grails-datastore-gorm-validation/src/main/groovy/grails/gorm/validation/ConstrainedProperty.groovy
     * https://github.com/grails/grails-data-mapping/tree/master/grails-datastore-gorm-validation/src/main/groovy/org/grails/datastore/gorm/validation/constraints
  * Dynamically add Validateable to command objects.
  * write GDSL for command objects.
  * Add Command object artefact porting over a versions of of constraints.
     * Static Map of maps for mapping [columnName:[constraintProperty: value]] rather than dynamic DSL
     * https://docs.grails.org/latest/ref/Constraints
     * blank
     * creditCard
     * email
     * inList
     * matches
     * max
     * maxSize
     * min
     * minSize
     * notEqual
     * nullable
     * range
     * scale
     * size
     * url
     * validator
  * Add constraints from the Grails Command plugin 
  * Implement a static checker for constraints into the command arefact(m2)
  * Implement Interceptors
  * Implement artefact traits
     * http://docs.grails.org/latest/guide/traits.html
  * Implement GDSL for artefact traits or write Intellij plugin
  * Add support for view rendering convention...
  * Add the ability to add artefacts using the conventions config.
  * Write cli/gradle tasks for generating artefacts.
  * Update documentation and add asciidoc
