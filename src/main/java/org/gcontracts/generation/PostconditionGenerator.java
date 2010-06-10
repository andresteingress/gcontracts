package org.gcontracts.generation;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.List;

/**
 * <p>
 * Code generator for postconditions.
 * </p>
 *
 * @author andre.steingress@gmail.com
 */
public class PostconditionGenerator extends BaseGenerator {

    public PostconditionGenerator(final ReaderSource source) {
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

        MapExpression oldVariableMap = new VariableGenerationUtility().generateOldVariablesMap(method);

        final BlockStatement methodBlock = (BlockStatement) method.getCode();

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        final List<Statement> statements = methodBlock.getStatements();
        if (statements.size() > 0)  {
            final BlockStatement postconditionCheck = new BlockStatement();

            if (method.getReturnType() != ClassHelper.VOID_TYPE)  {
                Statement lastStatement = statements.get(statements.size() - 1);

                ReturnStatement returnStatement = AssertStatementCreationUtility.getReturnStatement(method.getDeclaringClass(), method, lastStatement);

                statements.remove(statements.size() - 1);

                final AssertStatement assertStatement = AssertStatementCreationUtility.getAssertionStatement("postcondition", method, closureExpression);

                // backup the current assertion in a synthetic method
                AssertStatementCreationUtility.addAssertionMethodNode("postcondition", method, assertStatement, true, true);

                final MethodCallExpression methodCallToSuperPostcondition = AssertStatementCreationUtility.getMethodCallExpressionToSuperClassPostcondition(method, assertStatement.getLineNumber(), true, true);
                if (methodCallToSuperPostcondition != null) AssertStatementCreationUtility.addToAssertStatement(assertStatement, methodCallToSuperPostcondition, Token.newSymbol(Types.LOGICAL_AND, -1, -1));

                postconditionCheck.addStatement(assertStatement);

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

                methodBlock.addStatements(postconditionCheck.getStatements());
                methodBlock.addStatement(returnStatement);
            } else {

                final AssertStatement assertStatement = AssertStatementCreationUtility.getAssertionStatement("postcondition", method, closureExpression);
                // backup the current assertion in a synthetic method
                AssertStatementCreationUtility.addAssertionMethodNode("postcondition", method, assertStatement, true, false);

                final MethodCallExpression methodCallToSuperPostcondition = AssertStatementCreationUtility.getMethodCallExpressionToSuperClassPostcondition(method, assertStatement.getLineNumber(), true, false);

                if (methodCallToSuperPostcondition != null) AssertStatementCreationUtility.addToAssertStatement(assertStatement, methodCallToSuperPostcondition, Token.newSymbol(Types.LOGICAL_AND, -1, -1));

                postconditionCheck.addStatement(assertStatement);

                // Assign the return statement expression to a local variable of type Object
                VariableExpression oldVariable = new VariableExpression("old");
                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        oldVariableMap));

                postconditionCheck.getStatements().add(0, oldVariabeStatement);

                methodBlock.addStatements(postconditionCheck.getStatements());
            }
        }
    }

    /**
     * Adds a default postcondition if a postcondition has already been defined for this {@link org.codehaus.groovy.ast.MethodNode}
     * in a super-class.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode} of the given <tt>methodNode</tt>
     * @param methodNode the {@link org.codehaus.groovy.ast.MethodNode} to create the default postcondition for
     */
    public void generateDefaultPostconditionStatement(final ClassNode type, final MethodNode methodNode)  {

        // generate old variables -> are added as parameter with each inherited postcondition call
        final MapExpression oldVariableMap = new VariableGenerationUtility().generateOldVariablesMap(methodNode);
        final BlockStatement methodBlock = (BlockStatement) methodNode.getCode();

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        final List<Statement> statements = methodBlock.getStatements();
        if (statements.size() > 0)  {
            final BlockStatement postconditionCheck = new BlockStatement();

            if (methodNode.getReturnType() != ClassHelper.VOID_TYPE)  {
                Statement lastStatement = statements.get(statements.size() - 1);

                ReturnStatement returnStatement = AssertStatementCreationUtility.getReturnStatement(type, methodNode, lastStatement);
                statements.remove(statements.size() - 1);

                final MethodCallExpression methodCallToSuperPostcondition = AssertStatementCreationUtility.getMethodCallExpressionToSuperClassPostcondition(methodNode, methodNode.getLineNumber(), true, true);
                if (methodCallToSuperPostcondition == null) return;
                
                final AssertStatement assertStatement = new AssertStatement(new BooleanExpression(methodCallToSuperPostcondition));
                assertStatement.setLineNumber(methodNode.getLineNumber());
                postconditionCheck.addStatement(assertStatement);

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

                methodBlock.addStatements(postconditionCheck.getStatements());
                methodBlock.addStatement(returnStatement);
            } else {

                final MethodCallExpression methodCallToSuperPostcondition = AssertStatementCreationUtility.getMethodCallExpressionToSuperClassPostcondition(methodNode, methodNode.getLineNumber(), true, false);
                if (methodCallToSuperPostcondition == null) return;

                final AssertStatement assertStatement = new AssertStatement(new BooleanExpression(methodCallToSuperPostcondition));
                assertStatement.setLineNumber(methodNode.getLineNumber());
                postconditionCheck.addStatement(assertStatement);

                // Assign the return statement expression to a local variable of type Object
                VariableExpression oldVariable = new VariableExpression("old");
                ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        oldVariableMap));

                postconditionCheck.getStatements().add(0, oldVariabeStatement);

                methodBlock.addStatements(postconditionCheck.getStatements());
            }
        }
    }
}
