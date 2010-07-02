/**
 * Copyright (c) 2010, gcontracts@me.com
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
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Requires;
import org.gcontracts.ast.visitor.BaseVisitor;
import org.gcontracts.util.AnnotationUtils;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Central place to create {@link org.codehaus.groovy.ast.stmt.AssertStatement} instances in GContracts. Utilized
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

        final AssertStatement assertStatement = new AssertStatement(new BooleanExpression(expression), new ConstantExpression("[invariant] in class <" + classNode.getName() + "> violated"));
        assertStatement.setLineNumber(classNode.getLineNumber());
        
        return assertStatement;
    }

    /**
     * Gets the first {@link org.codehaus.groovy.ast.stmt.AssertStatement} found in the code of the given invariant {@link org.codehaus.groovy.ast.MethodNode}.
     *
     * @param methodNode the method which contains the class invariant
     * @return the {@link org.codehaus.groovy.ast.stmt.AssertStatement} found in the given <tt>methodNode</tt>
     */
    public static IfStatement getAssertStatementFromInvariantMethod(final MethodNode methodNode)  {

        final BlockStatement blockStatement = (BlockStatement) methodNode.getCode();
        return (IfStatement) blockStatement.getStatements().get(0);
    }

    /**
     * Reusable method for creating assert statements for the given <tt>closureExpression</tt>, injected in the
     * given <tt>method</tt> and with optional closure parameters.
     *
     * @param assertionType the name of the constraint, used for assertion messages
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @param closureExpression the assertion's {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     *
     * @return a new {@link org.codehaus.groovy.ast.stmt.IfStatement} which holds the assertion
     */
    public static IfStatement getAssertionStatement(final String assertionType, MethodNode method, ClosureExpression closureExpression) {

        final Expression expression = getFirstExpression(closureExpression);
        if (expression == null) throw new GroovyBugError("Assertion closure does not contain assertion expression!");

        final AssertStatement assertStatement = new AssertStatement(new BooleanExpression(expression), new ConstantExpression("[" + assertionType + "] in method <" + method.getName() + "(" + getMethodParameterString(method) + ")> violated"));
        assertStatement.setLineNumber(closureExpression.getLineNumber());

        final BlockStatement assertionBlockStatement = new BlockStatement();
        assertionBlockStatement.addStatement(assertStatement);

        return new IfStatement(new BooleanExpression(new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR)), assertionBlockStatement, new BlockStatement());
    }

    /**
     * Create a backup {@link org.codehaus.groovy.ast.MethodNode} with the given <tt>assertStatement</tt>. This method will be used
     * in descendants to call inherited assertions.
     *
     * @param assertionType the assertion type (precondition or postcondition)
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @param assertStatement the {@link org.codehaus.groovy.ast.stmt.AssertStatement} for which the backup should be created
     * @param withOldVariable indicates whether the old variable should be added as parameter
     * @param withResultVariable indicates whether the result variable should be added as parameter
     */
    public static void addAssertionMethodNode(final String assertionType, final MethodNode method, final AssertStatement assertStatement, boolean withOldVariable, boolean withResultVariable)  {

        // creates a new closure with all method parameters as closure parameters -> this is needed in descendants
        // e.g. when renaming of method parameter happens during redefinition of a method ...
        final BlockStatement methodBlockStatement = new BlockStatement();

        // copy the assert statement to provide a new message expression
        final BooleanExpression booleanExpression = new BooleanExpression(assertStatement.getBooleanExpression().getExpression());

        final BlockStatement assertBlockStatement = new BlockStatement();
        final AssertStatement newAssertStatement = new AssertStatement(booleanExpression);
        newAssertStatement.setLineNumber(assertStatement.getLineNumber());
        newAssertStatement.setColumnNumber(assertStatement.getColumnNumber());
        newAssertStatement.setLastColumnNumber(assertStatement.getLastColumnNumber());
        newAssertStatement.setLastLineNumber(assertStatement.getLastLineNumber());
        newAssertStatement.setMessageExpression(new ConstantExpression(((ConstantExpression) assertStatement.getMessageExpression()).getText().replaceFirst(assertionType, "inherited " + assertionType)));
        assertBlockStatement.addStatement(newAssertStatement);

        // add return value "true" so valid assertions in sub assertion statements get through
        methodBlockStatement.addStatement(new IfStatement(new BooleanExpression(new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR)), assertBlockStatement, new BlockStatement()));
        methodBlockStatement.addStatement(new ReturnStatement(ConstantExpression.TRUE));

        final ArrayList<Parameter> parameters = new ArrayList<Parameter>();
        parameters.addAll(Arrays.asList(method.getParameters()));

        if (withOldVariable) parameters.add(new Parameter(ClassHelper.OBJECT_TYPE, "old"));
        if (withResultVariable) parameters.add(new Parameter(method.getReturnType(), "result"));

        final ClassNode declaringClass = method.getDeclaringClass();
        final Parameter[] parameterArray = parameters.toArray(new Parameter[parameters.size()]);
        final String assertionMethodName = getAssertionMethodName(assertionType, method);

        if (method.getDeclaringClass().getSuperClass() != null && method.getDeclaringClass().getSuperClass().hasDeclaredMethod(assertionMethodName, parameterArray)) {
            final MethodCallExpression methodCallExpression = "precondition".equals(assertionType) ? getMethodCallExpressionToSuperClassPrecondition(method, newAssertStatement.getLineNumber()) : getMethodCallExpressionToSuperClassPostcondition(method, newAssertStatement.getLineNumber(), withOldVariable, withResultVariable);
            if (methodCallExpression != null)  {
                addToAssertStatement(newAssertStatement, methodCallExpression, "precondition".equals(assertionType) ? Token.newSymbol(Types.LOGICAL_OR, -1, -1) : Token.newSymbol(Types.LOGICAL_AND, -1, -1));
            }
        }

        final MethodNode preconditionMethodNode = declaringClass.addMethod(assertionMethodName, Opcodes.ACC_PROTECTED, ClassHelper.Boolean_TYPE, parameterArray, ClassNode.EMPTY_ARRAY, methodBlockStatement);
        preconditionMethodNode.setSynthetic(true);
    }

    /**
     * Looks up the next precondition in the class hierarchy of the given class and generates a method call on the previously
     * generated closure in the declaration expression.
     *
     * @param methodNode the current {@link org.codehaus.groovy.ast.MethodNode}, the lookup mechanism starts at the superclass of the declaring class node
     * @param lineNumber the line number of the current assertion
     *
     * @return a {@link org.codehaus.groovy.ast.expr.MethodCallExpression} to the inherited precondition
     */
    public static MethodCallExpression getMethodCallExpressionToSuperClassPrecondition(final MethodNode methodNode, final int lineNumber)  {

        final MethodNode nextMethodNode = AnnotationUtils.getMethodNodeInHierarchyWithAnnotation(methodNode, Requires.class);
        if (nextMethodNode == null) return null;

        final List<Expression> methodParameters = new ArrayList<Expression>();
        for (final Parameter param : methodNode.getParameters())  {
            methodParameters.add(new VariableExpression(param));
        }

        final MethodCallExpression methodCallExpression = new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, getAssertionMethodName("precondition", nextMethodNode), new ArgumentListExpression(methodParameters));
        methodCallExpression.setLineNumber(lineNumber);
        methodCallExpression.setSynthetic(true);

        return methodCallExpression;
    }

    /**
     * Looks up the next postcondition in the class hierarchy of the given class and generates a method call on the previously
     * generated closure in the declaration expression.
     *
     * @param methodNode the current {@link org.codehaus.groovy.ast.MethodNode}, the lookup mechanism starts at the superclass of the declaring class node
     * @param lineNumber the lineNumber of the current assertion
     * @param withOldVariable indicates whether the call needs an <tt>old</tt> parameter
     * @param withResultVariable indicates whether the call needs a <tt>result</tt> parameter
     *
     * @return a {@link org.codehaus.groovy.ast.expr.MethodCallExpression} to the inherited postcondition
     */
    public static MethodCallExpression getMethodCallExpressionToSuperClassPostcondition(final MethodNode methodNode, final int lineNumber, boolean withOldVariable, boolean withResultVariable)  {

        final MethodNode nextMethodNode = AnnotationUtils.getMethodNodeInHierarchyWithAnnotation(methodNode, Ensures.class);
        if (nextMethodNode == null) return null;

        final List<Expression> methodParameters = new ArrayList<Expression>();
        for (final Parameter param : methodNode.getParameters())  {
            methodParameters.add(new VariableExpression(param));
        }

        if (withOldVariable) methodParameters.add(new VariableExpression("old"));
        if (withResultVariable) methodParameters.add(new VariableExpression("result"));

        final MethodCallExpression methodCallExpression = new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, getAssertionMethodName("postcondition", nextMethodNode), new ArgumentListExpression(methodParameters));
        methodCallExpression.setLineNumber(lineNumber);
        methodCallExpression.setSynthetic(true);

        return methodCallExpression;
    }

    /**
     * Helper method to add an {@link org.codehaus.groovy.ast.expr.Expression} to the given {@link org.codehaus.groovy.ast.stmt.AssertStatement}. In fact,
     * a {@link org.codehaus.groovy.ast.expr.BinaryExpression} will be added where the two expressions are concatenated using the given <tt>booleanOperator</tt>.
     *
     * @param assertStatement the {@link org.codehaus.groovy.ast.stmt.AssertStatement} to be modified
     * @param expressionToAdd the {@link org.codehaus.groovy.ast.expr.Expression} to be added to the assert statement's expression
     * @param booleanOperator the {@link org.codehaus.groovy.syntax.Token} to be used for concatenation
     */
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
     * Creates the assertion method node name.
     *
     * @param assertionType the assertion type (precondition or postcondition)
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to create the assertion method name
     * @return the newly created assertion method name
     */
    private static String getAssertionMethodName(final String assertionType, final MethodNode method)  {
        return assertionType + "_" + method.getReturnType().getName().replaceAll("\\.", "_") + "_" + (method instanceof ConstructorNode ? "contructor" : method.getName());
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

    /**
     * Gets a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} from the given {@link org.codehaus.groovy.ast.stmt.Statement}.
     *
     * @param declaringClass the current {@link org.codehaus.groovy.ast.ClassNode} which declares the given method
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} that holds the given <tt>lastStatement</tt>
     * @param lastStatement the last {@link org.codehaus.groovy.ast.stmt.Statement} of some method code block
     * @return a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} or <tt>null</tt>
     */
    public static ReturnStatement getReturnStatement(ClassNode declaringClass, MethodNode method, Statement lastStatement)  {

        if (lastStatement instanceof ReturnStatement)  {
            return (ReturnStatement) lastStatement;
        } else if (lastStatement instanceof BlockStatement) {
            BlockStatement blockStatement = (BlockStatement) lastStatement;
            List<Statement> statements = blockStatement.getStatements();

            return statements.size() > 0 ? getReturnStatement(declaringClass, method, statements.get(statements.size() - 1)) : null;
        } else {
            if (!(lastStatement instanceof ExpressionStatement)) return null;
            // the last statement in a Groovy method could also be an expression which result is treated as return value
            ExpressionStatement expressionStatement = (ExpressionStatement) lastStatement;
            return new ReturnStatement(expressionStatement);
        }
    }
}
