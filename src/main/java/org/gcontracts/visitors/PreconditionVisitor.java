package org.gcontracts.visitors;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.injection.AssertStatementCreator;

/**
 * @author andre.steingress@gmail.com
 */
public class PreconditionVisitor extends BaseVisitor {

    public PreconditionVisitor(final ReaderSource source) {
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

        final BlockStatement preconditionCheck = AssertStatementCreator.getAssertionBlockStatement(method, closureExpression, "precondition", closureToSourceConverter.convertClosureExpressionToSourceCode(closureExpression, source));
        preconditionCheck.addStatement(method.getCode());

        method.setCode(preconditionCheck);
    }
}
