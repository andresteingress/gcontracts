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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.gcontracts.common.base.BaseAnnotationProcessor;
import org.gcontracts.common.spi.AnnotationProcessor;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.domain.ClassInvariant;
import org.gcontracts.domain.Contract;
import org.gcontracts.domain.Postcondition;
import org.gcontracts.util.ExpressionUtil;

/**
 * @author ast
 */
public class EnsuresAnnotationProcessor implements AnnotationProcessor {

    protected static final String CLOSURE_ATTRIBUTE_NAME = "value";

    @Override
    public void process(ProcessingContextInformation processingContextInformation, Contract contract, AnnotatedNode annotatedNode, AnnotationNode annotationNode) {
        if (!processingContextInformation.isPostconditionsEnabled()) return;

        BooleanExpression booleanExpression = ExpressionUtil.getBooleanExpression((ClosureExpression) annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME));
        if (booleanExpression == null) return;

        contract.addPostcondition((MethodNode) annotatedNode, new Postcondition(booleanExpression));
    }
}
