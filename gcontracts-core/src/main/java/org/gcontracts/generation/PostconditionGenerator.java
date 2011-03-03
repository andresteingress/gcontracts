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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.annotations.meta.Postcondition;
import org.gcontracts.ast.visitor.BaseVisitor;

import java.util.List;

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
        VariableGenerationUtility.addOldVariableMethodNode(classNode);
    }

    /**
     * Injects a postcondition assertion statement in the given <tt>method</tt>, based on the <tt>booleanExpression</tt>.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param booleanExpression the {@link org.codehaus.groovy.ast.expr.BooleanExpression} holding the assertion expression
     */
    public void generatePostconditionAssertionStatement(MethodNode method, BooleanExpression booleanExpression, boolean isConstructor)  {

        final BooleanExpression postconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Postcondition.class, booleanExpression, true);
        final BlockStatement blockStatement = wrapAssertionBooleanExpression(postconditionBooleanExpression);

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

        final BooleanExpression postconditionBooleanExpression = addCallsToSuperMethodNodeAnnotationClosure(method.getDeclaringClass(), method, Postcondition.class, new BooleanExpression(ConstantExpression.TRUE), true);
        if (postconditionBooleanExpression.getExpression() == ConstantExpression.TRUE) return;

        final BlockStatement blockStatement = wrapAssertionBooleanExpression(postconditionBooleanExpression);
        addPostcondition(method, blockStatement);
    }

    private void addPostcondition(MethodNode method, BlockStatement blockStatement) {
        final BlockStatement methodCode = ((BlockStatement) method.getCode());

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        final List<Statement> statements = methodCode.getStatements();
        if (statements.size() > 0)  {
            final BlockStatement postconditionCheck = new BlockStatement();

            if (method.getReturnType() != ClassHelper.VOID_TYPE)  {
                Statement lastStatement = statements.get(statements.size() - 1);

                ReturnStatement returnStatement = AssertStatementCreationUtility.getReturnStatement(method.getDeclaringClass(), method, lastStatement);
                if (returnStatement != null) statements.remove(statements.size() - 1);

                postconditionCheck.addStatements(blockStatement.getStatements());

                VariableExpression resultVariable = null;

                if (returnStatement != null)  {
                    // Assign the return statement expression to a local variable of type Object
                    resultVariable = new VariableExpression("result");
                    ExpressionStatement resultVariableStatement = new ExpressionStatement(
                    new DeclarationExpression(resultVariable,
                            Token.newSymbol(Types.ASSIGN, -1, -1),
                            returnStatement.getExpression()));

                    postconditionCheck.getStatements().add(0, resultVariableStatement);
                }

                // Assign the return statement expression to a local variable of type Object
                final VariableExpression oldVariableExpression = new VariableExpression("old");
                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new BinaryExpression(oldVariableExpression,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new MethodCallExpression(VariableExpression.THIS_EXPRESSION, VariableGenerationUtility.OLD_VARIABLES_METHOD, ArgumentListExpression.EMPTY_ARGUMENTS)));

                final BlockStatement oldVariableIfBlock = new BlockStatement();
                oldVariableIfBlock.addStatement(oldVariabeStatement);

                methodCode.getStatements().add(0, new ExpressionStatement(new DeclarationExpression(oldVariableExpression, Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)));
                methodCode.getStatements().add(1, new IfStatement(new BooleanExpression(new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR)), oldVariableIfBlock, new BlockStatement()));

                methodCode.addStatements(postconditionCheck.getStatements());

                if (returnStatement != null) methodCode.addStatement(new ReturnStatement(resultVariable));
            } else if (method instanceof ConstructorNode) {
                methodCode.addStatements(blockStatement.getStatements());

            } else {
                // Assign the return statement expression to a local variable of type Object
                final VariableExpression oldVariableExpression = new VariableExpression("old");
                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new BinaryExpression(oldVariableExpression,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new MethodCallExpression(VariableExpression.THIS_EXPRESSION, VariableGenerationUtility.OLD_VARIABLES_METHOD, ArgumentListExpression.EMPTY_ARGUMENTS)));

                final BlockStatement oldVariableIfBlock = new BlockStatement();
                oldVariableIfBlock.addStatement(oldVariabeStatement);

                methodCode.getStatements().add(0, new IfStatement(new BooleanExpression(new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR)), oldVariableIfBlock, new BlockStatement()));
                methodCode.getStatements().add(0, new ExpressionStatement(new DeclarationExpression(oldVariableExpression, Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.NULL)));

                methodCode.addStatements(blockStatement.getStatements());
            }
        }
    }
}
