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

import com.virtualdogbert.ast.MicroCompileStatic
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode

import javax.inject.Singleton
import java.util.regex.Pattern

/**
 * Grails artifact handler for service classes.
 *
 */
@CompileStatic
class ServiceArtefactHandler implements AstTrait {
    static final String REGEX_FILE_SEPARATOR = "[\\\\/]"
    static final String TYPE                 = "Service"

    /**
     * Checks to see if a class node is a service object. If debug mode is enabled through the config, then only the name of the classNode will be
     * checked an not the file path from the source units name.
     *
     * @param classNode The class node to check.
     * @param name the source unit name used to look up the file url.
     * @param config The conventions config to use.
     *
     * @return true if the class node is a service and false otherwise.
     */
    static boolean isArtefact(ClassNode classNode, String name, ConfigObject config) {
        if (classNode == null ||
            !classNode.getName().endsWith(TYPE)) {
            return false
        }

        if (config.debug && classNode.getName().endsWith(TYPE)) {
            return true
        }

        Pattern ServicePathPattern = Pattern.compile(".+${REGEX_FILE_SEPARATOR}$config.rootPath${REGEX_FILE_SEPARATOR}$config.servicePath${REGEX_FILE_SEPARATOR}(.+)\\.(groovy)")
        URL url = new File(name).toURL()

        return url && ServicePathPattern.matcher(url.getFile()).find()
    }

    /**
     * Handles applying the conventions to service objects, like adding @Transactional, @MicroCompileStatic, adding @Singleton, and doing service injection.
     *
     * @param classNode The class node for the service object.
     * @param config The conventions config to use.
     */
    static void handleNode(ClassNode classNode, ConfigObject config) {

        //Inject Services
        addServiceInjection(classNode, config)

        addAnnotation(classNode, Singleton, getBeanAnnotationNames())

        if (config.transactional && !((List<String>) config.transactionalExcludes).contains("${classNode.packageName}.$classNode.nameWithoutPackage".toString())) {
            addAnnotation(classNode, Transactional, [Transactional.name, NotTransactional.name])
        }

        if (config.compileStatic && !((List<String>) config.compileStaticExcludes).contains("${classNode.packageName}.$classNode.nameWithoutPackage".toString())) {
            addAnnotation(classNode, MicroCompileStatic, [CompileStatic.name, CompileDynamic.name, MicroCompileStatic.name])
        }
    }
}
