package com.virtualdogbert.extensions

import com.virtualdogbert.artefact.DomainArtefactHandler
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL
/**
 *
 * @since 2.4
 */
class DomainMappingTypeCheckingExtension extends TypeCheckingDSL {

    @Override
    public Object run() {
        setup { newScope() }

        finish { scopeExit() }

        beforeVisitClass { ClassNode classNode ->
            def mappingProperty = classNode.getField('mapping')
            if(mappingProperty && mappingProperty.isStatic() && mappingProperty.initialExpression instanceof ClosureExpression) {
                def sourceUnit = classNode?.module?.context
                if(DomainArtefactHandler.isArtefact(classNode, sourceUnit.name)) {
                    newScope {
                        mappingClosureCode = mappingProperty.initialExpression.code
                    }
                    mappingProperty.initialExpression.code = new EmptyStatement()
                }
            }
        }

        afterVisitClass { ClassNode classNode ->
            if(currentScope.mappingClosureCode) {
                def mappingProperty = classNode.getField('mapping')
                mappingProperty.initialExpression.code = currentScope.mappingClosureCode
                currentScope.checkingMappingClosure = true
                withTypeChecker { visitClosureExpression mappingProperty.initialExpression }
                scopeExit()
            }
        }

        methodNotFound { ClassNode receiver, String name, ArgumentListExpression argList, ClassNode[] argTypes, MethodCall call ->
            def dynamicCall
            if(currentScope.mappingClosureCode && currentScope.checkingMappingClosure) {
                dynamicCall = makeDynamic (call)
            }
            dynamicCall
        }

        null
    }
}