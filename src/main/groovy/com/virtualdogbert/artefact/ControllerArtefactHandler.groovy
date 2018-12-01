/*
 * Copyright (c) 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.virtualdogbert.artefact

import com.virtualdogbert.ast.MicroCompileStatic
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode

import java.util.regex.Pattern
/**
 * Grails artifact handler for command classes.
 *
 */
@CompileStatic
class ControllerArtefactHandler implements ErrorHandler, AstTrait {
    static final String       REGEX_FILE_SEPARATOR = "[\\\\/]"
    static final String       TYPE                 = "Controller"
    static final List<String> methodNames          = [Get.simpleName, Post.simpleName, Put.simpleName, Delete.simpleName, Patch.simpleName, Head.simpleName, Options.simpleName, Trace.simpleName]
    static final List<String> annotationNames      = [Get.name, Post.name, Put.name, Delete.name, Patch.name, Head.name, Options.name, Trace.name]

    static boolean isArtefact(ClassNode classNode, String name, ConfigObject config) {
        if (classNode == null ||
            !classNode.getName().endsWith(TYPE)) {
            return false
        }

        if (config.debug && classNode.getName().endsWith(TYPE)) {
            return true
        }

        Pattern ControllerPathPattern = Pattern.compile(".+${REGEX_FILE_SEPARATOR}$config.rootPath${REGEX_FILE_SEPARATOR}$config.controllerPath${REGEX_FILE_SEPARATOR}(.+)\\.(groovy)")
        URL url = new File(name).toURL()

        return url && ControllerPathPattern.matcher(url.getFile()).find()
    }

    static void handleNode(ClassNode classNode, ConfigObject urlMappings, ConfigObject config) {
        String controllerName = classNode.nameWithoutPackage.replace('Controller', '').uncapitalize()
        String urlMapping = getUrlMapping(classNode, urlMappings, 'url', controllerName)
        addAnnotation(classNode, Controller, [Controller.name], urlMapping)

        if (config.compileStatic && !((List<String>) config.compileStaticExcludes).contains("${classNode.packageName}.$classNode.nameWithoutPackage".toString())) {
            addAnnotation(classNode, MicroCompileStatic, [CompileStatic.name, CompileDynamic.name, MicroCompileStatic.name])
        }

        List<MethodNode> methods = classNode.methods

        //Inject Services
        addServiceInjection(classNode, config)

        //Apply url mappings
        for (MethodNode methodNode : methods) {
            if (methodNode.isPrivate()) {
                break
            }

            urlMapping = getUrlMapping(classNode, methodNode, (ConfigObject) urlMappings[controllerName], 'url', methodNode.name)
            String producesMapping = getUrlMapping(classNode,  methodNode, (ConfigObject) urlMappings[controllerName], 'produces', MediaType.APPLICATION_JSON)
            String method = getUrlMapping(classNode, methodNode, (ConfigObject) urlMappings[controllerName], 'method', methodNode.name).toLowerCase().capitalize()

            if (!(method in methodNames)) {
                throw new UrlMappingException("Method $method in urlMappings.groovy is not valid. Possible methods include $methodNames .", methodNode);
            }

            AnnotationNode methodAnnotation = new AnnotationNode(new ClassNode(Class.forName("io.micronaut.http.annotation.$method")))
            addAnnotation(methodNode, methodAnnotation, annotationNames, urlMapping)

            methodAnnotation = new AnnotationNode(new ClassNode(Produces.class))
            addAnnotation(methodNode, methodAnnotation, [Produces.name], producesMapping)
        }
    }

    static String getUrlMapping(ClassNode classNode, ConfigObject urlMapping, String key, String defaultMapping) {
        ConfigObject controllerMapping = (ConfigObject) urlMapping[defaultMapping]

        if (!controllerMapping) {
            throw new UrlMappingException("No mapping for controllerName: $defaultMapping, key name: $key, in UrlMapping.Groovy", classNode)
        }

        String mapping = controllerMapping[key]
        return mapping ?: defaultMapping
    }

    static String getUrlMapping(ClassNode classNode, MethodNode methodNode, ConfigObject urlMapping, String key, String defaultMapping) {
        ConfigObject controllerMapping = (ConfigObject) urlMapping[methodNode.name]

        if (!controllerMapping) {
            throw new UrlMappingException("No mapping for class name: $classNode.name, action name: $methodNode.name, key name: $key, in UrlMapping.Groovy", methodNode)
        }

        String mapping = controllerMapping[key]
        return mapping ?: defaultMapping
    }
}
