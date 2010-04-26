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
            builder.append(parameter.getName()).append(":").append(parameter.getType().getTypeClass().getName());
        }

        return builder.toString();
    }

}
