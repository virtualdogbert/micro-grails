package com.virtualdogbert.extensions

import com.virtualdogbert.artefact.DomainArtefactHandler
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL
/**
 *
 * @since 2.4
 */
class CriteriaTypeCheckingExtension extends TypeCheckingDSL {

    @Override
    public Object run() {
        setup { newScope() }

        finish { scopeExit() }

        methodNotFound { ClassNode receiver, String name, ArgumentListExpression argList, ClassNode[] argTypes, MethodCall call ->
            def dynamicCall
            if(currentScope.processingCriteriaClosure) {
                dynamicCall = makeDynamic (call)
            }
            dynamicCall
        }

        afterMethodCall { MethodCall call ->
            if(isCriteriaCall(call)) {
                scopeExit()
            }
        }

        beforeMethodCall { MethodCall call ->
            if(isCriteriaCall(call)) {
                newScope {
                    processingCriteriaClosure = true
                }
            }
        }
        null
    }

    protected boolean isCriteriaCall(MethodCall call) {
        call instanceof MethodCallExpression &&
        call.objectExpression instanceof ClassExpression &&
        DomainArtefactHandler.isArtefact(call.objectExpression.type, call.objectExpression.type.name) &&
        (call.method.value == 'withCriteria' || call.method.value == 'createCriteria')
    }
}