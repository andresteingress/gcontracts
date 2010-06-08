package org.gcontracts.generation;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.util.ClosureToSourceConverter;

/**
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

        final BlockStatement preconditionCheck = AssertStatementCreationUtility.getAssertionBlockStatement(method, closureExpression, "precondition", ClosureToSourceConverter.convert(closureExpression, source));
        preconditionCheck.addStatement(method.getCode());

        method.setCode(preconditionCheck);
    }
}
