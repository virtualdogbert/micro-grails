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
import grails.gorm.annotation.Entity
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode

import java.util.regex.Pattern
/**
 * Grails artifact handler for command classes.
 *
 */
@CompileStatic
class DomainArtefactHandler implements AstTrait {
    static final String REGEX_FILE_SEPARATOR = "[\\\\/]"
    static final String TYPE                 = "Domain"

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

        Pattern DomainPathPattern = Pattern.compile(".+${REGEX_FILE_SEPARATOR}$config.rootPath${REGEX_FILE_SEPARATOR}$config.domainPath${REGEX_FILE_SEPARATOR}(.+)\\.(groovy)")
        URL url = new File(name).toURL()

        return url && DomainPathPattern.matcher(url.getFile()).find()
    }

    static void handleNode(ClassNode classNode, ConfigObject config) {
        addAnnotation(classNode, Entity, [Entity.simpleName])

        if (config.compileStatic && !((List<String>) config.compileStaticExcludes).contains("${classNode.packageName}.$classNode.nameWithoutPackage".toString())) {
            addAnnotation(classNode, MicroCompileStatic, [CompileStatic.name, CompileDynamic.name, MicroCompileStatic.name])
        }
    }
}
