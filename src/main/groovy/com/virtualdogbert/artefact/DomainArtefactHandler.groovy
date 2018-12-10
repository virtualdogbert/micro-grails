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
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode

/**
 * Grails artifact handler for domain classes.
 *
 */
@CompileStatic
class DomainArtefactHandler implements AstTrait {
    static final String TYPE = "Domain"

    /**
     * Checks to see if a class node is a domain object.  If debug mode is enabled through the config, then only the name of the classNode will be
     * checked an not the file path from the source units name.
     *
     * @param classNode The class node to check.
     * @param name the source unit name used to look up the file url.
     * @param config The conventions config to use.
     *
     * @return true if the class node is a domain and false otherwise.
     */
    static boolean isArtefact(ClassNode classNode, String name, ConfigObject config = null) {

        if (classNode == null ||
            !classNode.getName().endsWith(TYPE)) {
            return false
        }

        if (!config) {
            if (!this.config) {
                this.config = getConventions()
            }

            config = this.config
        }

        if (config.debug && classNode.getName().endsWith(TYPE)) {
            return true
        }

        return name.contains("/$config.rootPath/$config.domainPath/") && name.endsWith("${TYPE}.groovy")
    }

    /**
     * Handles applying the conventions to domain objects, like adding @Entity, and @MicroCompileStatic.
     *
     * @param classNode The class node for the domain object.
     * @param config The conventions config to use.
     */
    static void handleNode(ClassNode classNode, ConfigObject config) {
        addAnnotation(classNode, Entity, [Entity.simpleName])

        if (config.compileStatic && !((List<String>) config.compileStaticExcludes).contains("${classNode.packageName}.$classNode.nameWithoutPackage".toString())) {
            addAnnotation(classNode, MicroCompileStatic, [CompileStatic.name, CompileDynamic.name, MicroCompileStatic.name])
        }
    }
}
