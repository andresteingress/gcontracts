/**
 * Copyright (c) 2010, gcontracts.lib@gmail.com
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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.powerassert.AssertionRewriter;
import org.gcontracts.annotations.Requires;
import org.gcontracts.util.AnnotationUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Central place to create {@link org.codehaus.groovy.ast.stmt.AssertStatement} instances in gcontracts. Utilized
 * to centralize {@link AssertionError} message generation.
 *
 * @see org.codehaus.groovy.ast.stmt.AssertStatement
 * @see AssertionError
 *
 * @author andre.steingress@gmail.com
 */
public final class AssertStatementCreationUtility {

    /**
     * Reusable method for creating assert statements for the given <tt>invariantField</tt>.
     *
     * @param classNode the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     *
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static AssertStatement getInvariantAssertionStatement(final ClassNode classNode, final ClosureExpression closureExpression)  {
        final Expression expression = getFirstExpression(closureExpression);
        if (expression == null) throw new GroovyBugError("Assertion closure does not contain assertion expression!");

        final AssertStatement assertStatement = new AssertStatement(new BooleanExpression(expression), new ConstantExpression("[invariant] Invariant in class <" + classNode.getName() + "> violated"));
        assertStatement.setLineNumber(classNode.getLineNumber());

        return assertStatement;
    }

    /**
     * Reusable method for creating assert statements for the given <tt>closureExpression</tt>, injected in the
     * given <tt>method</tt> and with optional closure parameters.
     *
     * @param assertionType the name of the constraint, used for assertion messages
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     *
     * @return a new {@link org.codehaus.groovy.ast.stmt.BlockStatement} which holds the assertion
     */
    public static AssertStatement getAssertionStatement(final String assertionType, MethodNode method, ClosureExpression closureExpression) {

        final Expression expression = getFirstExpression(closureExpression);
        if (expression == null) throw new GroovyBugError("Assertion closure does not contain assertion expression!");

        final AssertStatement assertStatement = new AssertStatement(new BooleanExpression(expression), new ConstantExpression("[" + assertionType + "] In method <" + method.getName() + "(" + getMethodParameterString(method) + ")> violated"));
        assertStatement.setLineNumber(closureExpression.getLineNumber());

        return assertStatement;
    }

    /**
     * Creates a {@link org.codehaus.groovy.ast.expr.DeclarationExpression} which wraps the given {@link org.codehaus.groovy.ast.stmt.AssertStatement} into
     * a closure with according parameters. This helper variable is needed in descendants to check for inherited assertions.
     *
     * @param assertionType
     * @param method
     * @param assertStatement
     * 
     * @return
     */
    public static DeclarationExpression getDeclarationExpression(final String assertionType, final MethodNode method, final AssertStatement assertStatement)  {

        // creates a new closure with all method parameters as closure parameters -> this is needed in descendants
        // e.g. when renaming of method parameter happens during redefinition of a method ...
        final BlockStatement closureBlockStatement = new BlockStatement();

        final AssertStatement newAssertStatement = new AssertStatement(assertStatement.getBooleanExpression());
        newAssertStatement.setLineNumber(assertStatement.getLineNumber());
        newAssertStatement.setColumnNumber(assertStatement.getColumnNumber());
        newAssertStatement.setLastColumnNumber(assertStatement.getLastColumnNumber());
        newAssertStatement.setLastLineNumber(assertStatement.getLastLineNumber());
        newAssertStatement.setMessageExpression(new ConstantExpression("[inherited]" + ((ConstantExpression) assertStatement.getMessageExpression()).getText()));

        closureBlockStatement.addStatement(newAssertStatement);
        closureBlockStatement.addStatement(new ReturnStatement(ConstantExpression.TRUE));

        final ClosureExpression closureExpression = new ClosureExpression(method.getParameters(), closureBlockStatement);
        closureExpression.setVariableScope(new VariableScope(method.getVariableScope()));
        closureExpression.setSynthetic(true);
        closureExpression.setLineNumber(assertStatement.getLineNumber());
        
        final DeclarationExpression declarationExpression = new DeclarationExpression(new VariableExpression("$" + assertionType + "$"), Token.newSymbol(Types.ASSIGN, -1, -1), closureExpression);
        declarationExpression.setSynthetic(true);
        declarationExpression.setLineNumber(assertStatement.getLineNumber());

        return declarationExpression;
    }

    public static MethodCallExpression getMethodCallExpressionToSuperClassPrecondition(final MethodNode methodNode, final AssertStatement assertStatement)  {

        final MethodNode nextMethodNode = AnnotationUtils.getMethodNodeInHierarchyWithAnnotation(methodNode, Requires.class);
        if (nextMethodNode == null) return null;

        BlockStatement methodBlockStatement = (BlockStatement) nextMethodNode.getCode();
        final ClosureExpression closureExpressionOfSuperPrecondition = getAssertionClosureExpression("precondition", methodBlockStatement);
        if (closureExpressionOfSuperPrecondition == null) return null;

        final List<Expression> closureVariables = new ArrayList<Expression>();
        for (final Parameter param : methodNode.getParameters())  {
            closureVariables.add(new VariableExpression(param));
        }

        final MethodCallExpression methodCallExpression = new MethodCallExpression(closureExpressionOfSuperPrecondition, "call", new ArgumentListExpression(new ListExpression(closureVariables)));
        methodCallExpression.setLineNumber(assertStatement.getLineNumber());
        methodCallExpression.setSynthetic(true);

        return methodCallExpression;
    }

    public static void addToAssertStatement(final AssertStatement assertStatement, final Expression expressionToAdd, final Token booleanOperator)  {

        final BooleanExpression booleanExpressionLeft = assertStatement.getBooleanExpression();
        final BinaryExpression binaryExpression = new BinaryExpression(
                booleanExpressionLeft.getExpression(),
                booleanOperator,
                expressionToAdd
        );
        final BooleanExpression newBooleanExpression = new BooleanExpression(binaryExpression);

        assertStatement.setBooleanExpression(newBooleanExpression);
    }

    /**
     * Creates a representative {@link String} of the given {@link org.codehaus.groovy.ast.MethodNode}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to create the representation
     * @return a {@link String} representation of the given <tt>method</tt>
     */
    private static String getMethodParameterString(MethodNode method)  {
        final StringBuilder builder = new StringBuilder();

        for (Parameter parameter : method.getParameters())  {
            if (builder.length() > 0)  {
                builder.append(", ");
            }
            builder.append(parameter.getName()).append(":").append(parameter.getType().getName());
        }

        return builder.toString();
    }

    /**
     * Returns the first {@link Expression} in the given {@link org.codehaus.groovy.ast.expr.ClosureExpression}.
     *
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     * @return the first {@link org.codehaus.groovy.ast.expr.Expression} found in the given {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     */
    private static Expression getFirstExpression(ClosureExpression closureExpression)  {
        final BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();
        final List<Statement> statementList = closureBlockStatement.getStatements();

        for (Statement stmt : statementList)  {
            if (stmt instanceof ExpressionStatement)  {
                return ((ExpressionStatement) stmt).getExpression();
            }
        }

        return null;
    }

    private static ClosureExpression getAssertionClosureExpression(final String assertionType, final BlockStatement methodBlockStatement)  {

        for (Statement statement : methodBlockStatement.getStatements())  {
            if (statement instanceof ExpressionStatement
                    && ((ExpressionStatement) statement).getExpression() instanceof DeclarationExpression
                    && ((DeclarationExpression) ((ExpressionStatement) statement).getExpression()).getLeftExpression() instanceof VariableExpression
                    && ((VariableExpression) ((DeclarationExpression) ((ExpressionStatement) statement).getExpression()).getLeftExpression()).getName().equals("$" + assertionType + "$"))  {


                return (ClosureExpression) ((DeclarationExpression) ((ExpressionStatement) statement).getExpression()).getRightExpression();
            }
        }

        return null;
    }
}
