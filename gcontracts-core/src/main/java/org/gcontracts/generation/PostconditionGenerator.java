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
import org.gcontracts.annotations.meta.Postcondition;
import org.gcontracts.ast.visitor.AnnotationClosureVisitor;
import org.gcontracts.ast.visitor.BaseVisitor;
import org.gcontracts.util.AnnotationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Code generator for postconditions.
 * </p>
 *
 * @author ast
 */
public class PostconditionGenerator extends BaseGenerator {

    public PostconditionGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Adds a synthetic method to the given <tt>classNode</tt> which can be used
     * to create a map of most instance variables found in this class. Used for the <tt>old</tt> variable
     * mechanism.
     *
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} to add the synthetic method to
     */
    public void addOldVariablesMethod(final ClassNode classNode)  {
        OldVariableGenerationUtility.addOldVariableMethodNode(classNode);
    }

    /**
     * Injects a postcondition assertion statement in the given <tt>method</tt>, based on the <tt>booleanExpression</tt>.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param postcondition the {@link org.gcontracts.domain.Postcondition} the assertion statement should be generated from
     */
    public void generatePostconditionAssertionStatement(MethodNode method, org.gcontracts.domain.Postcondition postcondition)  {

        final BooleanExpression postconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Postcondition.class, postcondition.booleanExpression(), true);


        BlockStatement blockStatement;
        final BlockStatement originalBlockStatement = postcondition.originalBlockStatement();
        // if use execution tracker flag is found in the meta-data the annotation closure visitor discovered
        // method calls which might be subject to cycling boolean expressions -> no inline mode possible
        final boolean useExecutionTracker = originalBlockStatement == null || Boolean.TRUE.equals(originalBlockStatement.getNodeMetaData(AnnotationClosureVisitor.META_DATA_USE_EXECUTION_TRACKER));

        if (!useExecutionTracker && Boolean.TRUE.equals(method.getNodeMetaData(META_DATA_USE_INLINE_MODE)))  {
            blockStatement = getInlineModeBlockStatement(originalBlockStatement);
        } else {
            blockStatement = wrapAssertionBooleanExpression(method.getDeclaringClass(), method, postconditionBooleanExpression, "postcondition");
        }

        addPostcondition(method, blockStatement);
    }

    /**
     * Adds a default postcondition if a postcondition has already been defined for this {@link org.codehaus.groovy.ast.MethodNode}
     * in a super-class.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode} of the given <tt>methodNode</tt>
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to create the default postcondition for
     */
    public void generateDefaultPostconditionStatement(final ClassNode type, final MethodNode method)  {

        // if another precondition is available we'll evaluate to false
        boolean isAnotherPostconditionAvailable = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), method, ClassHelper.makeWithoutCaching(Postcondition.class)).size() > 0;
        if (!isAnotherPostconditionAvailable) return;

        // if another post-condition is available we need to add a default expression of TRUE
        // since post-conditions are usually connected with a logical AND
        final BooleanExpression postconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Postcondition.class, new BooleanExpression(ConstantExpression.TRUE), true);
        if (postconditionBooleanExpression.getExpression() == ConstantExpression.TRUE) return;

        final BlockStatement blockStatement = wrapAssertionBooleanExpression(type, method, postconditionBooleanExpression, "postcondition");
        addPostcondition(method, blockStatement);
    }

    private void addPostcondition(MethodNode method, BlockStatement postconditionBlockStatement) {
        final BlockStatement methodCode = ((BlockStatement) method.getCode());

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        final List<Statement> statements = methodCode.getStatements();
        if (statements.size() > 0)  {
            VariableExpression enabledVariableExpression = new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR, ClassHelper.boolean_TYPE);
            enabledVariableExpression.setAccessedVariable(enabledVariableExpression);

            if (method.getReturnType() != ClassHelper.VOID_TYPE)  {
                List<ReturnStatement> returnStatements = AssertStatementCreationUtility.getReturnStatements(method);

                for (ReturnStatement returnStatement : returnStatements)  {
                    BlockStatement localPostconditionBlockStatement = new BlockStatement(new ArrayList<Statement>(postconditionBlockStatement.getStatements()), new VariableScope());

                    // Assign the return statement expression to a local variable of type Object
                    VariableExpression variableExpression = new VariableExpression("result", method.getReturnType());
                    variableExpression.setAccessedVariable(variableExpression);

                    ExpressionStatement resultVariableStatement = new ExpressionStatement(
                            new DeclarationExpression(variableExpression,
                                    Token.newSymbol(Types.ASSIGN, -1, -1),
                                    returnStatement.getExpression()));

                    localPostconditionBlockStatement.getStatements().add(0, resultVariableStatement);

                    AssertStatementCreationUtility.injectResultVariableReturnStatementAndAssertionCallStatement(methodCode, method.getReturnType().redirect(), returnStatement, localPostconditionBlockStatement);
                }

                // Assign the return statement expression to a local variable of type Object
                final VariableExpression oldVariableExpression = new VariableExpression("old", new ClassNode(Map.class));
                oldVariableExpression.setAccessedVariable(oldVariableExpression);

                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                        new BinaryExpression(oldVariableExpression,
                                Token.newSymbol(Types.ASSIGN, -1, -1),
                                new MethodCallExpression(VariableExpression.THIS_EXPRESSION, OldVariableGenerationUtility.OLD_VARIABLES_METHOD, ArgumentListExpression.EMPTY_ARGUMENTS)));

                final BlockStatement oldVariableIfBlock = new BlockStatement();
                oldVariableIfBlock.addStatement(oldVariabeStatement);

                methodCode.getStatements().add(0, new ExpressionStatement(new DeclarationExpression(oldVariableExpression, Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)));
                methodCode.getStatements().add(1, new IfStatement(new BooleanExpression(enabledVariableExpression), oldVariableIfBlock, new BlockStatement()));

            } else if (method instanceof ConstructorNode) {
                methodCode.addStatements(postconditionBlockStatement.getStatements());

            } else {
                // Assign the return statement expression to a local variable of type Object
                final VariableExpression oldVariableExpression = new VariableExpression("old", new ClassNode(Map.class));
                oldVariableExpression.setAccessedVariable(oldVariableExpression);

                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                        new BinaryExpression(oldVariableExpression,
                                Token.newSymbol(Types.ASSIGN, -1, -1),
                                new MethodCallExpression(VariableExpression.THIS_EXPRESSION, OldVariableGenerationUtility.OLD_VARIABLES_METHOD, ArgumentListExpression.EMPTY_ARGUMENTS)));

                final BlockStatement oldVariableIfBlock = new BlockStatement();
                oldVariableIfBlock.addStatement(oldVariabeStatement);

                methodCode.getStatements().add(0, new IfStatement(new BooleanExpression(enabledVariableExpression), oldVariableIfBlock, new BlockStatement()));
                methodCode.getStatements().add(0, new ExpressionStatement(new DeclarationExpression(oldVariableExpression, Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)));

                methodCode.addStatements(postconditionBlockStatement.getStatements());
            }
        }
    }
}
