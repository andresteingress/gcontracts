/**
 * Copyright (c) 2011, Andre Steingress
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.gcontracts.annotations.meta.AnnotationProcessorImplementation;
import org.gcontracts.annotations.meta.ContractElement;
import org.gcontracts.common.spi.AnnotationProcessor;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.Validate;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Visits annotations of meta-type {@link ContractElement} and applies the AST transformations of the underlying
 * {@link org.gcontracts.common.spi.AnnotationProcessor} implementation.
 *
 * @see org.gcontracts.common.spi.AnnotationProcessor
 *
 * @author ast
 */
public class AnnotationProcessorVisitor extends BaseVisitor {

    private ProcessingContextInformation pci;

    public AnnotationProcessorVisitor(final SourceUnit sourceUnit, final ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);
        Validate.notNull(pci);

        this.pci = pci;
    }

    @Override
    public void visitClass(ClassNode type) {
        handleClassNode(type);

        List<MethodNode> methodNodes = new ArrayList<MethodNode>();
        methodNodes.addAll(type.getMethods());
        methodNodes.addAll(type.getDeclaredConstructors());

        for (MethodNode methodNode : methodNodes)  {
            if (!CandidateChecks.isClassInvariantCandidate(type, methodNode) && !CandidateChecks.isPreOrPostconditionCandidate(type, methodNode)) continue;

            handleMethodNode(methodNode, AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName()));
        }

        // visit all interfaces of this class
        visitInterfaces(type, type.getInterfaces());
        visitAbstractBaseClassesForInterfaceMethodNodes(type, type.getSuperClass());
    }

    private void visitAbstractBaseClassesForInterfaceMethodNodes(ClassNode origin, ClassNode superClass) {
        if (superClass == null) return;
        if (!Modifier.isAbstract(superClass.getModifiers())) return;

        for (ClassNode interfaceClassNode : superClass.getInterfaces())  {
            List<MethodNode> methodNodes = new ArrayList<MethodNode>();
            methodNodes.addAll(interfaceClassNode.getMethods());

            for (MethodNode interfaceMethodNode : methodNodes)  {
                final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethodNode, ContractElement.class.getName());
                if (annotationNodes == null || annotationNodes.isEmpty()) continue;

                MethodNode implementingMethodNode = superClass.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());

                // if implementingMethodNode == null, then superClass is abstract and does not implement
                // the current interface methodNode
                if (implementingMethodNode != null) continue;

                MethodNode implementationInOriginClassNode = origin.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());
                if (implementationInOriginClassNode == null) continue;

                handleMethodNode(implementationInOriginClassNode, annotationNodes);
            }
        }
    }

    private void visitInterfaces(final ClassNode classNode, final ClassNode[] interfaces) {
        for (ClassNode interfaceClassNode : interfaces)  {
            List<MethodNode> methodNodes = new ArrayList<MethodNode>();
            methodNodes.addAll(interfaceClassNode.getMethods());

            // @ContractElement annotations are by now only supported on method interfaces
            for (MethodNode interfaceMethodNode : methodNodes)  {
                MethodNode implementingMethodNode = classNode.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());
                if (implementingMethodNode == null) continue;

                final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethodNode, ContractElement.class.getName());
                handleInterfaceMethodNode(classNode, implementingMethodNode, annotationNodes);
            }

            visitInterfaces(classNode, interfaceClassNode.getInterfaces());
        }
    }

    private void handleClassNode(final ClassNode classNode)  {
        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(classNode, ContractElement.class.getName());

        for (AnnotationNode annotationNode : annotationNodes)  {
            final AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);

            if (annotationProcessor != null && annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME) instanceof ClassExpression)  {
                final ClassExpression closureClassExpression = (ClassExpression) annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);

                ArgumentListExpression closureConstructorArgumentList = new ArgumentListExpression(
                        closureClassExpression,
                        new ArrayExpression(
                                ClassHelper.DYNAMIC_TYPE,
                                Arrays.<Expression>asList(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)
                        )
                );

                StaticMethodCallExpression methodCallExpression = new StaticMethodCallExpression(
                        ClassHelper.makeWithoutCaching(InvokerHelper.class),
                        "invokeConstructorOf",
                        closureConstructorArgumentList
                );

                MethodCallExpression doCall = new MethodCallExpression(
                        methodCallExpression,
                        "doCall",
                        ArgumentListExpression.EMPTY_ARGUMENTS
                );
                doCall.setMethodTarget(closureClassExpression.getType().getMethods("doCall").get(0));

                final BooleanExpression booleanExpression = new BooleanExpression(doCall);
                booleanExpression.setSourcePosition(annotationNode);

                annotationProcessor.process(pci, pci.contract(), classNode, booleanExpression);
            }
        }
    }

    private void handleInterfaceMethodNode(ClassNode type, MethodNode methodNode, List<AnnotationNode> annotationNodes) {
        handleMethodNode(type.getMethod(methodNode.getName(), methodNode.getParameters()), annotationNodes);
    }

    private void handleMethodNode(MethodNode methodNode, List<AnnotationNode> annotationNodes) {
        if (methodNode == null) return;

        for (AnnotationNode annotationNode : annotationNodes)  {
            final AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);

            if (annotationProcessor != null && annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME) instanceof ClassExpression)  {
                boolean isPostcondition = AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), org.gcontracts.annotations.meta.Postcondition.class.getName());

                ArgumentListExpression closureArgumentList = new ArgumentListExpression();

                for (Parameter parameter : methodNode.getParameters())  {
                    closureArgumentList.addExpression(new VariableExpression(parameter));
                }

                if (methodNode.getReturnType() != ClassHelper.VOID_TYPE && isPostcondition && !(methodNode instanceof ConstructorNode))  {
                    VariableExpression variableExpression = new VariableExpression("result", methodNode.getReturnType());
                    variableExpression.setAccessedVariable(variableExpression);

                    closureArgumentList.addExpression(variableExpression);
                }

                if (isPostcondition && !(methodNode instanceof ConstructorNode)) {
                    VariableExpression variableExpression = new VariableExpression("old", new ClassNode(Map.class));
                    variableExpression.setAccessedVariable(variableExpression);

                    closureArgumentList.addExpression(variableExpression);
                }

                ArgumentListExpression newInstanceArguments = new ArgumentListExpression(
                        annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME),
                                new ArrayExpression(
                                        ClassHelper.DYNAMIC_TYPE,
                                        Arrays.<Expression>asList(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)
                                )
                        );

                StaticMethodCallExpression methodCallExpression = new StaticMethodCallExpression(
                        ClassHelper.makeWithoutCaching(InvokerHelper.class),
                        "invokeConstructorOf",
                        newInstanceArguments
                );

                MethodCallExpression doCall = new MethodCallExpression(
                        methodCallExpression,
                        "doCall",
                        closureArgumentList
                );
                ClassNode type = annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME).getType();
                doCall.setMethodTarget(type.getMethods("doCall").get(0));

                final BooleanExpression booleanExpression = new BooleanExpression(doCall);
                booleanExpression.setSourcePosition(annotationNode);

                annotationProcessor.process(pci, pci.contract(), methodNode.getDeclaringClass(), methodNode, booleanExpression);

                // if the implementation method has no annotation, we need to set a dummy marker in order to find parent pre/postconditions
                if (!AnnotationUtils.hasAnnotationOfType(methodNode, annotationNode.getClassNode().getName()))  {
                    AnnotationNode annotationMarker = new AnnotationNode(annotationNode.getClassNode());
                    annotationMarker.setMember(CLOSURE_ATTRIBUTE_NAME, annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME));
                    annotationMarker.setRuntimeRetention(true);
                    annotationMarker.setSourceRetention(false);

                    methodNode.addAnnotation(annotationMarker);
                }
            }
        }
    }

    private AnnotationProcessor createAnnotationProcessor(AnnotationNode annotationNode) {
        final AnnotationProcessorImplementation annotationProcessingAnno = (AnnotationProcessorImplementation) annotationNode.getClassNode().getTypeClass().getAnnotation(AnnotationProcessorImplementation.class);
        if (annotationProcessingAnno == null) return null;

        final Class clz = annotationProcessingAnno.value();

        try {
            return (AnnotationProcessor) clz.newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {}

        return null;
    }
}
