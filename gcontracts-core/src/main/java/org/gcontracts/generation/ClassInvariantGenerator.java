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
package org.gcontracts.generation;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.annotations.meta.ClassInvariant;
import org.gcontracts.ast.visitor.BaseVisitor;
import org.gcontracts.util.AnnotationUtils;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * <p>
 * Code generator for class invariants.
 * </p>
 *
 * @author ast
 */
public class ClassInvariantGenerator extends BaseGenerator {

    public ClassInvariantGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Reads the {@link org.gcontracts.annotations.Invariant} boolean expression and generates a synthetic
     * method holding this class invariant. This is used for heir calls to find out about inherited class
     * invariants.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param classInvariant the {@link org.codehaus.groovy.ast.expr.BooleanExpression} containing the assertion expression
     */
    public void generateInvariantAssertionStatement(final ClassNode type, final BooleanExpression classInvariant)  {

        BooleanExpression classInvariantExpression = addCallsToSuperAnnotationClosure(type, ClassInvariant.class, classInvariant);

        final BlockStatement blockStatement = new BlockStatement();

        // add a local protected method with the invariant closure - this is needed for invariant checks in inheritance lines
        MethodNode methodNode = type.addMethod(getInvariantMethodName(type), Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.Boolean_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, blockStatement);
        blockStatement.addStatements(wrapAssertionBooleanExpression(type, methodNode, classInvariantExpression, "invariant").getStatements());
        blockStatement.addStatement(new ReturnStatement(ConstantExpression.TRUE));
    }

    private BooleanExpression addCallsToSuperAnnotationClosure(final ClassNode type, final Class<? extends Annotation> annotationType, BooleanExpression booleanExpression)  {

        final List<AnnotationNode> nextContractElementAnnotations = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), ClassHelper.makeWithoutCaching(annotationType));
        if (nextContractElementAnnotations.isEmpty()) return booleanExpression;

        for (AnnotationNode nextContractElementAnnotation : nextContractElementAnnotations)  {
            ClassExpression classExpression = (ClassExpression) nextContractElementAnnotation.getMember(BaseVisitor.CLOSURE_ATTRIBUTE_NAME);
            if (classExpression == null) continue;

            ArgumentListExpression closureConstructorArgumentList = new ArgumentListExpression(
                    VariableExpression.THIS_EXPRESSION,
                    VariableExpression.THIS_EXPRESSION);

            MethodCallExpression doCall = new MethodCallExpression(
                    new MethodCallExpression(
                            classExpression,
                            "newInstance",
                            closureConstructorArgumentList
                    ),
                    "call",
                    ArgumentListExpression.EMPTY_ARGUMENTS
            );

            final BooleanExpression rightExpression = new BooleanExpression(doCall);
            booleanExpression.setSourcePosition(nextContractElementAnnotation);

            booleanExpression = new BooleanExpression(
                    new BinaryExpression(
                            booleanExpression,
                            Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                            rightExpression)
            );
        }

        return booleanExpression;
    }

    /**
     * Adds the current class-invariant to the given <tt>method</tt>.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} which declared the given {@link org.codehaus.groovy.ast.MethodNode}
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     */
    public void addInvariantAssertionStatement(final ClassNode type, final MethodNode method)  {

        final String invariantMethodName = getInvariantMethodName(type);
        final MethodNode invariantMethod = type.getDeclaredMethod(invariantMethodName, Parameter.EMPTY_ARRAY);
        if (invariantMethod == null) return;

        Statement invariantMethodCall = new ExpressionStatement(
                new MethodCallExpression(
                        VariableExpression.THIS_EXPRESSION,
                        invariantMethod.getName(),
                        ArgumentListExpression.EMPTY_ARGUMENTS
                )
        );

        final Statement statement = method.getCode();
        if (statement instanceof BlockStatement && method.getReturnType() != ClassHelper.VOID_TYPE && !(method instanceof ConstructorNode))  {
            final BlockStatement blockStatement = (BlockStatement) statement;

            final List<ReturnStatement> returnStatements = AssertStatementCreationUtility.getReturnStatements(method);
            for (ReturnStatement returnStatement : returnStatements)  {
                AssertStatementCreationUtility.addAssertionCallStatementToReturnStatement (blockStatement, returnStatement, invariantMethodCall);
            }

            if (returnStatements.isEmpty()) blockStatement.addStatement(invariantMethodCall);

        } else if (statement instanceof BlockStatement) {
            final BlockStatement blockStatement = (BlockStatement) statement;
            blockStatement.addStatement(invariantMethodCall);
        } else {
            final BlockStatement assertionBlock = new BlockStatement();
            assertionBlock.addStatement(statement);
            assertionBlock.addStatement(invariantMethodCall);

            method.setCode(assertionBlock);
        }
    }
}
