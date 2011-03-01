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
import org.codehaus.groovy.control.messages.Message;
import org.gcontracts.annotations.meta.AnnotationProcessorImplementation;
import org.gcontracts.annotations.meta.ContractElement;
import org.gcontracts.common.spi.AnnotationProcessor;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.ExpressionUtils;
import org.gcontracts.util.Validate;

import java.util.ArrayList;
import java.util.List;

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
        if (!CandidateChecks.isContractsCandidate(type)) return;

        visitInterfaces(type, type.getInterfaces());

        visitAnnotatedNode(type, null, null);

        List<MethodNode> methodNodes = new ArrayList<MethodNode>();
        methodNodes.addAll(type.getMethods());
        methodNodes.addAll(type.getDeclaredConstructors());

        for (MethodNode methodNode : methodNodes)  {
            visitAnnotatedNode(methodNode, type, null);

            for (Parameter parameter : methodNode.getParameters())  {
                visitAnnotatedNode(parameter, type, methodNode);
            }
        }
    }

    private void visitInterfaces(ClassNode classNode, ClassNode[] interfaces) {
        for (ClassNode interfaceClassNode : interfaces)  {
            List<MethodNode> methodNodes = new ArrayList<MethodNode>();
            methodNodes.addAll(interfaceClassNode.getMethods());

            // @ContractElement annotations are by now only supported on method interfaces
            for (MethodNode interfaceMethodNode : methodNodes)  {
                MethodNode implementingMethodNode = classNode.getMethod(interfaceMethodNode.getName(), interfaceMethodNode.getParameters());
                if (implementingMethodNode == null) continue;

                final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(interfaceMethodNode, ContractElement.class.getName());
                for (AnnotationNode annotationNode : annotationNodes)  {
                    final AnnotationProcessor annotationProcessor = createAnnotationProcessor(annotationNode);

                    if (annotationProcessor != null && annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME) instanceof ClassExpression)  {

                        ArgumentListExpression closureConstructorArgumentList = new ArgumentListExpression(
                                VariableExpression.THIS_EXPRESSION,
                                VariableExpression.THIS_EXPRESSION);

                        ArgumentListExpression closureArgumentList = new ArgumentListExpression();

                        for (Parameter parameter : implementingMethodNode.getParameters())  {
                            closureArgumentList.addExpression(new VariableExpression(parameter));
                        }

                        closureArgumentList.addExpression(new VariableExpression("result"));
                        closureArgumentList.addExpression(new VariableExpression("old"));

                        MethodCallExpression doCall = new MethodCallExpression(
                                new MethodCallExpression(
                                        annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME),
                                        "newInstance",
                                        closureConstructorArgumentList
                                ),
                                "call",
                                closureArgumentList
                        );

                        annotationProcessor.process(pci, pci.contract(), implementingMethodNode, new BooleanExpression(doCall));
                    }
                }
            }
        }
    }

    private void visitAnnotatedNode(AnnotatedNode annotatedNode, ClassNode classNode, MethodNode methodNode) {
        Validate.notNull(annotatedNode);

        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(annotatedNode, ContractElement.class.getName());

        for (AnnotationNode annotationNode : annotationNodes)  {
            AnnotationProcessor processor = createAnnotationProcessor(annotationNode);
            if (processor == null) continue;

            if (methodNode == null)  {
                processor.process(pci, pci.contract(), annotatedNode, ExpressionUtils.getBooleanExpression((ClosureExpression) annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME)));
            } else {
                processor.process(pci, pci.contract(), annotatedNode, methodNode, ExpressionUtils.getBooleanExpression((ClosureExpression) annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME)));
            }
        }
    }

    private AnnotationProcessor createAnnotationProcessor(AnnotationNode annotationNode) {
        final AnnotationProcessorImplementation annotationProcessingAnno = (AnnotationProcessorImplementation) annotationNode.getClassNode().getTypeClass().getAnnotation(AnnotationProcessorImplementation.class);
        final Class clz = annotationProcessingAnno.value();

        try {
            return (AnnotationProcessor) clz.newInstance();
        } catch (InstantiationException e) {
            getSourceUnit().getErrorCollector().addError(Message.create("Could not instantiate " + clz, getSourceUnit()), false);
        } catch (IllegalAccessException e) {
            getSourceUnit().getErrorCollector().addError(Message.create("Could not access " + clz, getSourceUnit()), false);
        }

        return null;
    }
}
