/**
 * Copyright (c) 2010, gcontracts@me.com
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
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.Message;
import org.gcontracts.annotations.meta.ContractElement;
import org.gcontracts.common.spi.AnnotationProcessingASTTransformation;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Visits annotations of meta-type {@link ContractElement} and applies the AST transformations of the underlying
 * {@link AnnotationProcessingASTTransformation} implementation.
 *
 * @see AnnotationProcessingASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
public class AnnotationProcessingASTTransformationsVisitor extends BaseVisitor {

    private ProcessingContextInformation pci;

    public AnnotationProcessingASTTransformationsVisitor(final SourceUnit sourceUnit, final ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);
        Validate.notNull(pci);

        this.pci = pci;
    }

    protected AnnotationProcessingASTTransformationsVisitor() {}

    @Override
    public void visitClass(ClassNode type) {
        //addConfigurationVariable(type);
        visitAnnotatedNode(type, null, null);

        List<MethodNode> methodNodes = new ArrayList<MethodNode>();
        methodNodes.addAll(type.getAllDeclaredMethods());
        methodNodes.addAll(type.getDeclaredConstructors());

        for (MethodNode methodNode : methodNodes)  {
            visitAnnotatedNode(methodNode, type, null);

            for (Parameter parameter : methodNode.getParameters())  {
                visitAnnotatedNode(parameter, type, methodNode);
            }
        }
    }

    private void visitAnnotatedNode(AnnotatedNode annotatedNode, ClassNode classNode, MethodNode methodNode) {
        Validate.notNull(annotatedNode);

        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(annotatedNode, ContractElement.class.getName());

        for (AnnotationNode annotationNode : annotationNodes)  {
            final org.gcontracts.annotations.meta.AnnotationProcessingASTTransformation annotationProcessingAnno = (org.gcontracts.annotations.meta.AnnotationProcessingASTTransformation) annotationNode.getClassNode().getTypeClass().getAnnotation(org.gcontracts.annotations.meta.AnnotationProcessingASTTransformation.class);
            Class<? extends AnnotationProcessingASTTransformation>[] classes = annotationProcessingAnno.value();

            for (Class<? extends AnnotationProcessingASTTransformation> clz : classes)  {
                try {
                    final AnnotationProcessingASTTransformation processor = clz.newInstance();
                    if (annotatedNode instanceof ClassNode)  {
                        processor.process(pci, (ClassNode) annotatedNode);
                    } else if (annotatedNode instanceof MethodNode)  {
                        MethodNode annotatedMethodNode = (MethodNode) annotatedNode;
                        processor.process(pci, classNode, annotatedMethodNode);
                    } else if (annotatedNode instanceof Parameter)  {
                        processor.process(pci, classNode, methodNode, (Parameter) annotatedNode);
                    }
                } catch (InstantiationException e) {
                    getSourceUnit().getErrorCollector().addError(Message.create("Could not instantiate " + clz, getSourceUnit()), false);
                } catch (IllegalAccessException e) {
                    getSourceUnit().getErrorCollector().addError(Message.create("Could not access " + clz, getSourceUnit()), false);
                }
            }
        }
    }
}
