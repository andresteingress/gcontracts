/**
 * Copyright (c) 2013, Andre Steingress
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
package org.gcontracts.generation;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.meta.Precondition;
import org.gcontracts.ast.visitor.AnnotationClosureVisitor;
import org.gcontracts.util.AnnotationUtils;

/**
 * Code generator for preconditions.
 *
 * @author ast
 */
public class PreconditionGenerator extends BaseGenerator {

    public PreconditionGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Injects a precondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link org.gcontracts.annotations.Requires}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param precondition the {@link org.gcontracts.domain.Precondition} the assertion statement should be generated from
     */
    public void generatePreconditionAssertionStatement(final MethodNode method, final org.gcontracts.domain.Precondition precondition)  {
        final BooleanExpression preconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Precondition.class, precondition.booleanExpression(), false);

        BlockStatement blockStatement;

        final BlockStatement originalBlockStatement = precondition.originalBlockStatement();
        // if use execution tracker flag is found in the meta-data the annotation closure visitor discovered
        // method calls which might be subject to cycling boolean expressions -> no inline mode possible
        final boolean useExecutionTracker = originalBlockStatement == null || Boolean.TRUE.equals(originalBlockStatement.getNodeMetaData(AnnotationClosureVisitor.META_DATA_USE_EXECUTION_TRACKER));

        if (!useExecutionTracker && Boolean.TRUE.equals(method.getNodeMetaData(META_DATA_USE_INLINE_MODE)))  {
            blockStatement = getInlineModeBlockStatement(precondition.originalBlockStatement());
        } else {
            blockStatement = wrapAssertionBooleanExpression(method.getDeclaringClass(), method, preconditionBooleanExpression, "precondition");
        }

        addPrecondition(method, blockStatement);
    }

    /**
     * Generates the default precondition statement for {@link org.codehaus.groovy.ast.MethodNode} instances with
     * the {@link org.gcontracts.annotations.meta.Precondition} annotation.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param methodNode the {@link org.codehaus.groovy.ast.MethodNode} with a {@link org.gcontracts.annotations.meta.Precondition} annotation
     */
    public void generateDefaultPreconditionStatement(final ClassNode type, final MethodNode methodNode)  {

        // if another precondition is available we'll evaluate to false
        boolean isAnotherPreconditionAvailable = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), methodNode, ClassHelper.makeWithoutCaching(Precondition.class)).size() > 0;
        if (!isAnotherPreconditionAvailable) return;

        // if there is another preconditio up the inheritance path, we need a default precondition with FALSE
        // e.g. C1 <no precondition> : C2 <item != null> == false || item != null
        BooleanExpression preconditionBooleanExpression = new BooleanExpression(ConstantExpression.FALSE);
        preconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(type, methodNode, Precondition.class, preconditionBooleanExpression, false);
        // if precondition could not be found in parent class, let's return
        if (preconditionBooleanExpression.getExpression() == ConstantExpression.FALSE)
            return;

        final BlockStatement blockStatement = wrapAssertionBooleanExpression(type,  methodNode, preconditionBooleanExpression, "precondition");
        
        addPrecondition(methodNode, blockStatement);
    }

    private void addPrecondition(MethodNode method, BlockStatement blockStatement) {
        final BlockStatement modifiedMethodCode = new BlockStatement();
        modifiedMethodCode.addStatements(blockStatement.getStatements());

        if (method.getCode() instanceof BlockStatement)  {

            BlockStatement methodBlock = (BlockStatement) method.getCode();
            for (Statement statement : methodBlock.getStatements())  {
                if (method instanceof ConstructorNode && statement instanceof ExpressionStatement && ((ExpressionStatement) statement).getExpression() instanceof ConstructorCallExpression)  {
                    modifiedMethodCode.getStatements().add(0, statement);
                } else {
                    modifiedMethodCode.getStatements().add(statement);
                }
            }
        } else {
            modifiedMethodCode.addStatement(method.getCode());
        }

        method.setCode(modifiedMethodCode);
    }
}
