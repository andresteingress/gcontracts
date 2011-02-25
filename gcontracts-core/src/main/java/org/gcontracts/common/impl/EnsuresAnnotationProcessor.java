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
package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.common.base.BaseAnnotationProcessingASTTransformation;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.PostconditionGenerator;
import org.gcontracts.util.Validate;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class EnsuresAnnotationProcessor extends BaseAnnotationProcessingASTTransformation {

    protected static final String CLOSURE_ATTRIBUTE_NAME = "value";

    @Override
    public void process(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
        Validate.notNull(processingContextInformation);
        Validate.notNull(classNode);
        Validate.notNull(methodNode);

        if (!processingContextInformation.isPostconditionsEnabled()) return;

        addPostcondition(processingContextInformation, classNode, methodNode);
    }

    public void addPostcondition(final ProcessingContextInformation processingContextInformation, final ClassNode type, final MethodNode method) {
        if (!CandidateChecks.isPreOrPostconditionCandidate(type, method)) return;

        final ReaderSource source = processingContextInformation.readerSource();
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        List<AnnotationNode> annotations = method.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                postconditionGenerator.generatePostconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                processingContextInformation.postconditionMethodNodes().add(method);
            }
        }
    }
}
