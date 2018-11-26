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
 * Global AST to add Validateable to command objects
 */

package com.virtualdogbert.ast

import com.virtualdogbert.artefact.*
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class GrailsConventionsASTTransformation extends AbstractASTTransformation implements AstTrait {
    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (sourceUnit.name == 'embedded_script_in_groovy_Ant_task' || sourceUnit.name.startsWith('GStringTemplateScript')) {
            return
        }

        this.sourceUnit = sourceUnit

        if (setupConfig) {
            setupConfig = false
            config = getConventions()
            urlMapping = getUrlMappings()
        }

        List<ClassNode> classes = sourceUnit.getAST().getClasses()

        classes.each { ClassNode node ->
            try {
                if (CommandArtefactHandler.isArtefact(node, sourceUnit.name, getConfig())) {
                    CommandArtefactHandler.handleNode(node, sourceUnit)
                }

                if (ControllerArtefactHandler.isArtefact(node, sourceUnit.name, getConfig())) {
                    ControllerArtefactHandler.handleNode(node, urlMapping, getConfig())
                }

                if (DomainArtefactHandler.isArtefact(node, sourceUnit.name, getConfig())) {
                    DomainArtefactHandler.handleNode(node, sourceUnit, getConfig())
                }

                if (ServiceArtefactHandler.isArtefact(node, sourceUnit.name, getConfig())) {
                    ServiceArtefactHandler.handleNode(node, getConfig())
                }
            } catch (UrlMappingException e) {
                addError(e.message, e.expression)
            }
        }
    }
}
