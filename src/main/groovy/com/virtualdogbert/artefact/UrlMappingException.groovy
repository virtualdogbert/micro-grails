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

import org.codehaus.groovy.ast.ASTNode

/**
 * An Exception for handling url mapping errors
 */
class UrlMappingException extends RuntimeException {
    /**
     * A message for the url mapping error
     */
    String  message

    /**
     * The ASTNode expression that the error is attached to.
     */
    ASTNode expression

    /**
     *  An Exception for handling url mapping errors
     *
     * @param message A message for the url mapping error
     * @param expression  The ASTNode expression that the error is attached to.
     */
    UrlMappingException(String message, ASTNode expression){
        super(message)
        this.message = message
        this.expression = expression
    }
}
