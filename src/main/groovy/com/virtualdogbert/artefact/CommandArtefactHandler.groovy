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
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.SourceUnit

/**
 * Grails artifact handler for command classes.
 *
 */
@CompileStatic
class CommandArtefactHandler {
    static final String TYPE = "Command"

    /**
     * A check to see if a class node is a command object
     *
     * @param classNode The class node to check.
     * @param name The name of the
     * @param config
     * @return
     */
    static boolean isArtefact(ClassNode classNode, String name, ConfigObject config) {
        if (classNode == null ||
            !classNode.getName().endsWith(TYPE)) {
            return false
        }

        if (config.debug && classNode.getName().endsWith(TYPE)) {
            return true
        }

        return name.contains("/$config.rootPath/$config.commandPath/") && name.endsWith("${TYPE}.groovy")
    }

    static void handleNode(ClassNode classNode, SourceUnit sourceUnit) {
        if (!classNode.interfaces*.name.contains('Validateable')) {
            //TODO for M2
            //node.addInterface(new ClassNode(Validateable))
        }
    }
}
