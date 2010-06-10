package org.gcontracts.generation;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

/**
 * Code generator for preconditions.
 *
 * @author andre.steingress@gmail.com
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
     * @param closureExpression the {@link org.codehaus.groovy.ast.expr.ClosureExpression} containing the assertion expression
     */
    public void generatePreconditionAssertionStatement(MethodNode method, ClosureExpression closureExpression)  {

        final BlockStatement modifiedMethodCode = new BlockStatement();
        final AssertStatement assertStatement = AssertStatementCreationUtility.getAssertionStatement("precondition", method, closureExpression);
        final ExpressionStatement expressionStatement = new ExpressionStatement(AssertStatementCreationUtility.getDeclarationExpression("precondition", method, assertStatement, false, false));
        final MethodCallExpression methodCallToSuperPrecondition = AssertStatementCreationUtility.getMethodCallExpressionToSuperClassPrecondition(method, assertStatement);

        if (methodCallToSuperPrecondition != null) AssertStatementCreationUtility.addToAssertStatement(assertStatement, methodCallToSuperPrecondition, Token.newSymbol(Types.LOGICAL_OR, -1, -1));

        assertStatement.setLineNumber(closureExpression.getLineNumber());
        expressionStatement.setLineNumber(closureExpression.getLineNumber());

        modifiedMethodCode.addStatement(assertStatement);
        modifiedMethodCode.addStatement(expressionStatement);

        modifiedMethodCode.addStatement(method.getCode());

        method.setCode(modifiedMethodCode);
    }
}
