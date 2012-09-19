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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.gcontracts.common.spi.AnnotationProcessor;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.domain.Contract;
import org.gcontracts.domain.Postcondition;
import org.gcontracts.domain.Precondition;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.Validate;

import java.util.List;

/**
 * <p>Implementation of a generic processor with handles {@link org.gcontracts.annotations.meta.AnnotationContract}
 * annotations and injects the given closure into the specified assertion.</p>
 *
 * @see org.gcontracts.annotations.meta.AnnotationContract
 *
 * @author ast
 */
public class AnnotationContractProcessor extends AnnotationProcessor {

    private final AnnotationNode annotationNode;
    private final List<ClassExpression> closureClasses;

    public AnnotationContractProcessor(AnnotationNode annotationNode, List<ClassExpression> closureClasses)  {
        Validate.notNull(annotationNode);
        Validate.notNull(closureClasses);

        this.annotationNode = annotationNode;
        this.closureClasses = closureClasses;
    }

    @Override
    public void process(ProcessingContextInformation processingContextInformation, Contract contract, ClassNode classNode, MethodNode methodNode, Parameter parameter) {
        for (ClassExpression closureClass : closureClasses)  {
            ArgumentListExpression closureConstructorArgumentList = new ArgumentListExpression(
                    VariableExpression.THIS_EXPRESSION,
                    VariableExpression.THIS_EXPRESSION);

            MethodCallExpression methodCallExpression = new MethodCallExpression(
                    closureClass,
                    "newInstance",
                    closureConstructorArgumentList
            );

            MethodCallExpression doCall = new MethodCallExpression(
                    methodCallExpression,
                    "call",
                    new ArgumentListExpression(new VariableExpression(parameter))
            );

            BooleanExpression booleanExpression = new BooleanExpression(doCall);
            booleanExpression.setSourcePosition(annotationNode);

            if (AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), org.gcontracts.annotations.meta.Precondition.class.getName()))  {
                contract.preconditions().join(methodNode, new Precondition(booleanExpression));
            } else if (AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), org.gcontracts.annotations.meta.Postcondition.class.getName()))  {
                contract.postconditions().join(methodNode, new Postcondition(booleanExpression, methodNode instanceof ConstructorNode));
            }
        }
    }
}
