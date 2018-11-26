package com.virtualdogbert.extensions

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL

/**
 *
 * @since 3.3
 */
class NamedQueryTypeCheckingExtension extends TypeCheckingDSL {

    @Override
    public Object run() {
        setup { newScope() }

        finish { scopeExit() }

        beforeVisitClass { ClassNode classNode ->
            def namedQueryProperty = classNode.getField('namedQueries')

            if(namedQueryProperty && namedQueryProperty.isStatic() && namedQueryProperty.initialExpression instanceof ClosureExpression) {
                newScope {
                    namedQueryClosureCode = namedQueryProperty.initialExpression.code
                }
                namedQueryProperty.initialExpression.code = new EmptyStatement()
            } else {
                newScope()
            }
        }

        afterVisitClass { ClassNode classNode ->
            if(currentScope.namedQueryClosureCode) {
                def namedQueryProperty = classNode.getField('namedQueries')
                namedQueryProperty.initialExpression.code = currentScope.namedQueryClosureCode
                currentScope.checkingNamedQueryClosure = true
                scopeExit()
            }
        }

        methodNotFound { ClassNode receiver, String name, ArgumentListExpression argList, ClassNode[] argTypes, MethodCall call ->
            def dynamicCall
            if(currentScope.namedQueryClosureCode && currentScope.checkingNamedQueryClosure) {
                dynamicCall = makeDynamic (call)
            }
            dynamicCall
        }

        null
    }
}
