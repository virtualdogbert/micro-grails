package com.virtualdogbert.artefact

import org.codehaus.groovy.ast.ASTNode

class UrlMappingException extends RuntimeException {
    String  message
    ASTNode expression

    UrlMappingException(String message, ASTNode expression){
        super(message)
        this.message = message
        this.expression = expression
    }
}
