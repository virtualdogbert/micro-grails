package com.virtualdogbert.extensions

import com.virtualdogbert.artefact.DomainArtefactHandler
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.MethodCall
import org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport.TypeCheckingDSL

import static org.codehaus.groovy.ast.ClassHelper.*
/**
 *
 * @since 2.4
 */
class DynamicFinderTypeCheckingExtension extends TypeCheckingDSL {

    @Override
    public Object run() {
        methodNotFound { ClassNode receiver, String name, ArgumentListExpression argList, ClassNode[] argTypes, MethodCall call ->
            def dynamicCall
            if(receiver == CLASS_Type) {
                def genericsTypes = receiver.genericsTypes
                if(genericsTypes) {
                    ClassNode staticMethodCallTargetType = genericsTypes[0].type
                    if(staticMethodCallTargetType) {
                        def sourceUnit = staticMethodCallTargetType?.module?.context
                        if(DomainArtefactHandler.isArtefact(staticMethodCallTargetType, sourceUnit.name)) {
                            switch(name) {
                                case ~/countBy[A-Z].*/:
                                    dynamicCall = makeDynamicGormCall(call, Integer_TYPE, staticMethodCallTargetType)
                                    break
                                case ~/findAllBy[A-Z].*/:
                                case ~/listOrderBy[A-Z].*/:
                                    def returnType = parameterizedType(LIST_TYPE, staticMethodCallTargetType)
                                    dynamicCall = makeDynamicGormCall(call, returnType, staticMethodCallTargetType)
                                    break
                                case ~/findBy[A-Z].*/:
                                case ~/findOrCreateBy[A-Z].*/:
                                case ~/findOrSaveBy[A-Z].*/:
                                    dynamicCall = makeDynamicGormCall(call, staticMethodCallTargetType, staticMethodCallTargetType)
                                    break
                            }
                        }
                    }
                }
            }
            return dynamicCall
        }
        null
    }

    protected makeDynamicGormCall(MethodCall call, ClassNode returnTypeNode, ClassNode domainClassTypeNode) {
        def dynamicCall = makeDynamic(call, returnTypeNode)
        dynamicCall.declaringClass = domainClassTypeNode
        dynamicCall
    }
}
