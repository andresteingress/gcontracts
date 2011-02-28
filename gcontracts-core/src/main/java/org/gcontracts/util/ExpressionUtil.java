package org.gcontracts.util;

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.List;

public class ExpressionUtil {

    /**
     * Returns the first {@link org.codehaus.groovy.ast.expr.BooleanExpression} in the given {@link org.codehaus.groovy.ast.expr.ClosureExpression}.
     *
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     * @return the first {@link org.codehaus.groovy.ast.expr.Expression} found in the given {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     */
    public static BooleanExpression getBooleanExpression(ClosureExpression closureExpression)  {
        final BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();
        final List<Statement> statementList = closureBlockStatement.getStatements();

        for (Statement stmt : statementList)  {
            if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof BooleanExpression)  {
                return (BooleanExpression) ((ExpressionStatement) stmt).getExpression();
            } else if (stmt instanceof ExpressionStatement)  {
                BooleanExpression result = new BooleanExpression(((ExpressionStatement) stmt).getExpression());
                result.setSourcePosition(stmt);
                return result;
            }
        }

        return null;
    }
}
