package org.gcontracts.visitors;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.injection.AssertStatementCreator;
import org.gcontracts.injection.VariableGenerator;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class PostconditionVisitor extends BaseVisitor {

    public PostconditionVisitor(final ReaderSource source) {
        super(source);
    }

        /**
     * Injects a postcondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link org.gcontracts.annotations.Ensures}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param closureExpression the {@link org.codehaus.groovy.ast.expr.ClosureExpression} containing the assertion expression
     */
    public void generatePostconditionAssertionStatement(MethodNode method, ClosureExpression closureExpression)  {

        // check whether the closure uses a result or old variable
        boolean usesResultVariable = false;
        boolean usesOldVariable = false;
        boolean usesResultVariableFirst = false;

        for (Parameter closureParameter : closureExpression.getParameters())  {
            if (closureParameter.getName().equals("old"))  {
                usesOldVariable = true;
            } else if (closureParameter.getName().equals("result"))  {
                usesResultVariable = true;
                usesResultVariableFirst = !usesOldVariable;
            }
        }

        MapExpression oldVariableMap = new MapExpression();

        if (usesOldVariable)  oldVariableMap = new VariableGenerator().generateOldVariablesMap(method);

        final BlockStatement methodBlock = (BlockStatement) method.getCode();

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        final List<Statement> statements = methodBlock.getStatements();
        if (statements.size() > 0)  {
            BlockStatement postconditionCheck = null;

            if (method.getReturnType() != ClassHelper.VOID_TYPE && usesResultVariable)  {
                Statement lastStatement = statements.get(statements.size() - 1);

                ReturnStatement returnStatement = getReturnStatement(lastStatement);

                statements.remove(statements.size() - 1);

                postconditionCheck = AssertStatementCreator.getAssertionBlockStatement(method, closureExpression, "postcondition", closureToSourceConverter.convertClosureExpressionToSourceCode(closureExpression, source));

                // Assign the return statement expression to a local variable of type Object
                VariableExpression resultVariable = new VariableExpression("result");
                ExpressionStatement resultVariableStatement = new ExpressionStatement(
                new DeclarationExpression(resultVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        returnStatement.getExpression()));

                postconditionCheck.getStatements().add(0, resultVariableStatement);

                // Assign the return statement expression to a local variable of type Object
                VariableExpression oldVariable = new VariableExpression("old");
                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        oldVariableMap));

                postconditionCheck.getStatements().add(0, oldVariabeStatement);

                methodBlock.addStatement(postconditionCheck);
                methodBlock.addStatement(returnStatement);
            } else {

                postconditionCheck = AssertStatementCreator.getAssertionBlockStatement(method, closureExpression, "postcondition", closureToSourceConverter.convertClosureExpressionToSourceCode(closureExpression, source));

                // Assign the return statement expression to a local variable of type Object
                VariableExpression oldVariable = new VariableExpression("old");
                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        oldVariableMap));

                postconditionCheck.getStatements().add(0, oldVariabeStatement);

                methodBlock.addStatement(postconditionCheck);
            }
        }
    }

    /**
     * Gets a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} from the given {@link org.codehaus.groovy.ast.stmt.Statement}.
     *
     * @param lastStatement the last {@link org.codehaus.groovy.ast.stmt.Statement} of some method code block
     * @return a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} or <tt>null</tt>
     */
    private ReturnStatement getReturnStatement(Statement lastStatement)  {

        if (lastStatement instanceof ReturnStatement)  {
            return (ReturnStatement) lastStatement;
        } else if (lastStatement instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) lastStatement;
            List<Statement> statements = blockStatement.getStatements();

            return statements.size() > 0 ? getReturnStatement(statements.get(statements.size() - 1)) : null;
        } else {
            // the last statement in a Groovy method could also be an expression which result is treated as return value
            ExpressionStatement expressionStatement = (ExpressionStatement) lastStatement;
            return new ReturnStatement(expressionStatement);
        }
    }
}
