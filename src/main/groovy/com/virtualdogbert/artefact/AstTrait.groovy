package com.virtualdogbert.artefact

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Prototype
import io.micronaut.runtime.context.scope.ThreadLocal
import jdk.internal.org.objectweb.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.syntax.Token
import org.codehaus.groovy.syntax.Types

import javax.inject.Singleton
import java.util.regex.Pattern

@CompileStatic
trait AstTrait {
    static final String       conventionsFile     = "conventions.groovy"
    static       ConfigObject config              = null
    static       ConfigObject urlMapping          = null
    static       boolean      setupConfig         = true
    static final List<String> beanAnnotationNames = [Singleton.name, Context.name, Prototype.name, ThreadLocal.name]


    static ConfigObject getConventions() {
        ConfigSlurper configSlurper = new ConfigSlurper()
        (ConfigObject) configSlurper.parse(new File(conventionsFile).toURL()).conventions
    }

    static ConfigObject getUrlMappings(){
        ConfigSlurper configSlurper = new ConfigSlurper()
        String rootPath = "${getConfig().rootPath}/${getConfig().controllerPath}"
        String urlMappings =  "${(String)getConfig().urlMappings}.groovy"
        configSlurper.parse(new File(getURL(rootPath, urlMappings )).toURL())
    }

    static String getURL(String base, String fileName) {
        Pattern pattern = ~/${fileName}/
        new FileNameByRegexFinder().getFileNames(base, pattern.toString())[0]
    }

    /**
     * Adds an annotation to a class node, if it doesn't already have that annotation, with a constant value.
     *
     * @param classNode The class node to add th annotation to.
     * @param annotation The class of the annotation to add.
     * @param value The optional value to set for the annotation
     */
    static void addAnnotation(ClassNode classNode, Class annotation, List<String> annotations, String value = null) {
        if (!(classNode.annotations*.classNode.name.any { String n -> n in annotations })) {

            AnnotationNode classAnnotation = new AnnotationNode(new ClassNode(annotation))

            if (value) {
                classAnnotation.addMember('value', (Expression) (new ConstantExpression(value)))
            }

            classNode.addAnnotation(classAnnotation)
        }
    }

    /**
     * Adds an annotation to a class node, if it doesn't already have that annotation, with a constant value.
     *
     * @param classNode The class node to add th annotation to.
     * @param annotation The class of the annotation to add.
     * @param value The optional value to set for the annotation
     */
    static void addAnnotation(MethodNode methodNode, AnnotationNode annotationNode, List<String> annotations, String value = null) {
        if (!(methodNode.annotations*.classNode.name.any { String n -> n in annotations })) {

            if (value) {
                annotationNode.addMember('value', (Expression) (new ConstantExpression(value)))
            }

            methodNode.addAnnotation(annotationNode)
        }
    }

    /**
     * This looks through the properties and fields of a classNode, and finds Fields/Properties with classes, that have annotations that are
     * considered injectable(See beanAnnotationNames above.). With  those  Fields/Properties a constructor is created for those Fields/Properties.
     *
     * @param classNode
     */
    static void addServiceInjection(ClassNode classNode) {
        if (classNode.declaredConstructors.size() == 0) {
            List<PropertyNode> properties = classNode.properties
            List<FieldNode> fields = classNode.fields
            List<Parameter> services = []
            BlockStatement constructor = new BlockStatement()

            for (PropertyNode property : properties) {
                List<AnnotationNode> annotations = property.type.annotations

                if (annotations*.classNode.name.any { String n -> n in getBeanAnnotationNames() }) {
                    Parameter service = new Parameter(property.type, property.name)
                    services << service

                    constructor.addStatement(
                            new ExpressionStatement(
                                    new BinaryExpression(
                                            new PropertyExpression(
                                                    new VariableExpression('this'),
                                                    new ConstantExpression(property.name)
                                            ),
                                            Token.newSymbol(Types.EQUAL, 0, 0),
                                            new VariableExpression(property.name, property.type)
                                    )
                            )
                    )
                }
            }


            for (FieldNode field : fields) {
                List<AnnotationNode> annotations = field.type.annotations

                if (annotations*.classNode.name.any { String n -> n in getBeanAnnotationNames() }) {
                    Parameter service = new Parameter(field.type, field.name)
                    services << service

                    constructor.addStatement(
                            new ExpressionStatement(
                                    new BinaryExpression(
                                            new PropertyExpression(
                                                    new VariableExpression('this'),
                                                    new ConstantExpression(field.name)
                                            ),
                                            Token.newSymbol(Types.EQUAL, 0, 0),
                                            new VariableExpression(field.name, field.type)
                                    )
                            )
                    )
                }
            }

            // Add injection constructor
            classNode.addConstructor(new ConstructorNode(Opcodes.ACC_PUBLIC, services.toArray(new Parameter[services.size()]), [] as ClassNode[], constructor))
        }
    }
}
