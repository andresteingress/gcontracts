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
package org.gcontracts.injection;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

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
public final class AssertStatementCreator {

    /**
     * Reusable method for creating assert statements for the given <tt>invariantField</tt>.
     *
     * @param classNode the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param invariantField the {@link org.codehaus.groovy.ast.FieldNode} pointing to the invariant closure field
     *
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static AssertStatement getInvariantAssertionStatement(final ClassNode classNode, final FieldNode invariantField)  {
        return new AssertStatement(new BooleanExpression(
            new MethodCallExpression(new FieldExpression(invariantField), "call", ArgumentListExpression.EMPTY_ARGUMENTS)
        ), new ConstantExpression("[invariant] Invariant in class <" + classNode.getName() + "> violated"));
    }

    /**
     * Reusable method for creating assert statements for the given <tt>closureExpression</tt>, injected in the
     * given <tt>method</tt> and with optional closure parameters.
     *
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     * @param constraint the name of the constraint, used for assertion messages
     * @param optionalParameters expressions to be used as closure parameters
     *
     * @return a new {@link org.codehaus.groovy.ast.stmt.BlockStatement} which holds the assertion
     */
    public static BlockStatement getAssertionBlockStatement(MethodNode method, ClosureExpression closureExpression, String constraint, Expression... optionalParameters) {
        final BlockStatement assertionBlock = new BlockStatement();
        // assign the closure to a local variable and call() it
        final VariableExpression closureVariable = new VariableExpression("$" + constraint + "Closure");

        // create a local variable to hold a reference to the newly instantiated closure
        assertionBlock.addStatement(new ExpressionStatement(
                new DeclarationExpression(closureVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        closureExpression)));

        final List<Expression> expressions = new ArrayList<Expression>(Arrays.asList(optionalParameters));

        assertionBlock.addStatement(new AssertStatement(new BooleanExpression(
                new MethodCallExpression(closureVariable, "call", new ArgumentListExpression(expressions))
        ), new ConstantExpression("[" + constraint + "] In method <" + method.getName() + "(" + getMethodParameterString(method) + ")> violated")));

        return assertionBlock;
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

}
