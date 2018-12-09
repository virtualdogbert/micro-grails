/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package com.virtualdogbert.artefact

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Prototype
import io.micronaut.runtime.context.scope.ThreadLocal
import jdk.internal.org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import javax.inject.Singleton
import java.util.regex.Pattern

/**
 * A trait to provide some common AST features dealing with configuration, conventions, and injections.
 */
@CompileStatic
trait AstTrait {
    static final String       conventionsFile     = "conventions.groovy"
    static       ConfigObject config              = null
    static       ConfigObject urlMapping          = null
    static       boolean      setupConfig         = true
    static final List<String> beanAnnotationNames = [Singleton.name, Context.name, Prototype.name, ThreadLocal.name]

    /**
     * Gets the conventions configurations for the library. This is called by the library itself, but also through CompileStatic extensions..
     *
     * @return A config object that holds the conventions.
     */
    static ConfigObject getConventions() {
        ConfigSlurper configSlurper = new ConfigSlurper()
        (ConfigObject) configSlurper.parse(new File(conventionsFile).toURI().toURL()).conventions
    }

    /**
     * This gets the url mapping as a config object. This is something that I'm open to replacing with a better DSL, but I didn't want to
     * delay Milestone 1, to have a better implementation, which could come from someone else through a pull request.
     *
     * @return the url mapping as a config object
     */
    static ConfigObject getUrlMappings() {
        ConfigSlurper configSlurper = new ConfigSlurper()
        String rootPath = "${getConfig().rootPath}/${getConfig().controllerPath}"
        String urlMappings = "${(String) getConfig().urlMappings}.groovy"
        configSlurper.parse(new File(getURL(rootPath, urlMappings)).toURI().toURL())
    }

    /**
     * This is used by the url mapping to file the url mapping file, because I don't know ahead of time where is will be package wise. so this
     * is used to find the filename based on the configured root of the project(grails-app, micronaut-app,etc), and the urlMappings file name.
     *
     * @param base The root of the project (grails-app, micronaut-app,etc)
     * @param fileName The name of the file to find, in a package structure.
     *
     * @return The filename as a string.
     */
    static String getURL(String base, String fileName) {
        Pattern pattern = ~/${fileName}/
        new FileNameByRegexFinder().getFileNames(base, pattern.toString())[0]
    }

    /**
     * Checks a class node to see if it has an annotation from a list of annotations.
     *
     * @param classNode The method node to check.
     * @param annotations The list of annotations to check against.
     *
     * @return true if the class node as an annotation is the list to check, else false
     */
    static boolean hasAnnotation(ClassNode classNode, List<String> annotations){
        classNode.annotations*.classNode.name.any { String n -> n in annotations }
    }

    /**
     * Checks a method node to see if it has an annotation from a list of annotations.
     *
     * @param methodNode The method node to check.
     * @param annotations The list of annotations to check against.
     *
     * @return true if the method node as an annotation is the list to check, else false
     */
    static boolean hasAnnotation(MethodNode methodNode, List<String> annotations) {
        methodNode.annotations*.classNode.name.any { String n -> n in annotations }
    }

    /**
     * Adds an annotation to a class node, if it doesn't already have that annotation, with a constant value.
     *
     * @param classNode The class node to add th annotation to.
     * @param annotation The class of the annotation to add.
     * @param value The optional value to set for the annotation
     */
    static void addAnnotation(ClassNode classNode, Class annotation, List<String> annotations, String value = null) {
        if (!hasAnnotation(classNode, annotations)) {

            AnnotationNode classAnnotation = new AnnotationNode(new ClassNode(annotation))

            if (value) {
                classAnnotation.addMember('value', (Expression) (new ConstantExpression(value)))
            }

            classNode.addAnnotation(classAnnotation)
        }
    }

    /**
     * Adds an annotation to a class node, if it doesn't already have that annotation, with a constant value.
     *
     * @param classNode The class node to add th annotation to.
     * @param annotation The class of the annotation to add.
     * @param value The optional value to set for the annotation
     */
    static void addAnnotation(MethodNode methodNode, AnnotationNode annotationNode, List<String> annotations, String value = null) {
        if (!hasAnnotation(methodNode, annotations)) {

            if (value) {
                annotationNode.addMember('value', (Expression) (new ConstantExpression(value)))
            }

            methodNode.addAnnotation(annotationNode)
        }
    }

    /**
     * This looks through the properties and fields of a classNode, and finds Fields/Properties with classes, that have annotations that are
     * considered injectable(See beanAnnotationNames above.). With  those  Fields/Properties a constructor is created for those Fields/Properties.
     *
     * @param classNode the class node to add injection to.
     * @param config The conventions config that will be used by isArtefact checking for services to inject.
     */
    static void addServiceInjection(ClassNode classNode, ConfigObject config) {
        if (classNode.declaredConstructors.size() == 0) {
            List<PropertyNode> properties = classNode.properties
            List<FieldNode> fields = classNode.fields
            List<Parameter> services = []
            BlockStatement constructor = new BlockStatement()

            for (PropertyNode property : properties) {

                if (ServiceArtefactHandler.isArtefact(property.type, property.type.name, config)) {
                    services << new Parameter(property.type, property.name)

                    constructor.addStatement(
                            createAssignmentStatement(classNode, property.name, property.type)
                    )
                }
            }


            for (FieldNode field : fields) {

                if (ServiceArtefactHandler.isArtefact(field.type, field.type.name, config)) {
                    services << new Parameter(field.type, field.name)

                    constructor.addStatement(
                            createAssignmentStatement(classNode, field.name, field.type)
                    )
                }
            }

            ConstructorNode constructorNode = new ConstructorNode(Opcodes.ACC_PUBLIC, services.toArray(new Parameter[services.size()]), [] as ClassNode[], constructor)

            // Add injection constructor
            classNode.addConstructor(constructorNode)
        }
    }

    /**
     * Crates an expression statement for assigning this.service = service.
     *
     * @param classNode the parent classNode that the statement is being assigned.
     * @param serviceName the name of the service.
     * @param serviceType the ClassNode type of the service.
     *
     * @return The expression statement for this.service = service
     */
    static ExpressionStatement createAssignmentStatement(ClassNode classNode, String serviceName, ClassNode serviceType) {
        new ExpressionStatement(
                new BinaryExpression(
                        new PropertyExpression(
                                new VariableExpression('this'),
                                new ConstantExpression(serviceName)
                        ),
                        Token.newSymbol(Types.EQUAL, classNode.getLineNumber(), classNode.getColumnNumber()),
                        new VariableExpression(serviceName, serviceType)
                )
        )
    }
}
