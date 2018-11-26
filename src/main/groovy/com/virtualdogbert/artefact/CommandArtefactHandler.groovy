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

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit

import java.util.regex.Pattern

/**
 * Grails artifact handler for command classes.
 *
 */
@CompileStatic
class CommandArtefactHandler {
    static final String REGEX_FILE_SEPARATOR = "[\\\\/]"
    static final String TYPE                 = "Command"

    static boolean isArtefact(ClassNode classNode, String name, ConfigObject config) {
        if (classNode == null ||
            !classNode.getName().endsWith(TYPE)) {
            return false
        }

        if (config.debug && classNode.getName().endsWith(TYPE)) {
            return true
        }

        Pattern CommandPathPattern = Pattern.compile(".+${REGEX_FILE_SEPARATOR}$config.rootPath${REGEX_FILE_SEPARATOR}$config.command${REGEX_FILE_SEPARATOR}(.+)\\.(groovy)")
        URL url = new File(name).toURL()

        return url && CommandPathPattern.matcher(url.getFile()).find()
    }

    static void handleNode(ClassNode classNode, SourceUnit sourceUnit) {
        if (!classNode.interfaces*.name.contains('Validateable')) {
            //node.addInterface(new ClassNode(Validateable))
        }
    }
}