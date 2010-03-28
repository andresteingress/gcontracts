/**
Copyright (c) 2010, andre.steingress@gmail.com
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
disclaimer.
2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
disclaimer in the documentation and/or other materials provided with the distribution.
3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
**/
package org.gcontracts.ast;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom AST transformation that removes closure annotations of {@link org.gcontracts.annotations.Invariant},
 * {@link org.gcontracts.annotations.Requires} and {@link org.gcontracts.annotations.Ensures} and adds Java
 * assertions which executing the constraint-code instead. <p/>
 * Whenever a constraint is broken an {@link AssertionError} will be thrown.
 *
 * @see AssertionError
 *
 * @author andre.steingress@gmail.com
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class ContractValidationASTTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit unit) {
        ModuleNode moduleNode = (ModuleNode)nodes[0];
        for (ClassNode classNode : moduleNode.getClasses())

            new AssertionInjector(classNode).rewrite();
    }
}

/**
 * Injects standard Java assertion statements which check the specified pre- and post-conditions and the class
 * invariants.
 */
class AssertionInjector {

    private static final String CLOSURE_ATTRIBUTE_NAME = "value";

    private final ClassNode classNode;
    private ClosureExpression classInvariant;

    public AssertionInjector(ClassNode classNode) {
        this.classNode = classNode;
    }

    public void rewrite() {
        new ClassCodeVisitorSupport() {

            @Override
            public void visitClass(ClassNode type) {

                List<AnnotationNode> annotations = type.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getTypeClass() == Invariant.class)  {
                        generateInvariantAssertionStatement(type, annotation);
                    }
                }

                super.visitClass(type);
            }

            @Override
            public void visitMethod(MethodNode method) {

                super.visitMethod(method);

                List<AnnotationNode> annotations = method.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getTypeClass() == Requires.class)  {
                        generatePreconditionAssertionStatement(method, annotation);
                    } else if (annotation.getClassNode().getTypeClass() == Ensures.class)  {
                        generatePostconditionAssertionStatement(method, annotation);
                    }
                }

                // If there is a class invariant we will append the check to this invariant
                // after each method call
                if (classInvariant != null)  {
                    generateInvariantAssertionStatement(method);
                }
            }

            protected SourceUnit getSourceUnit() {
                return null;
            }
        }.visitClass(classNode);
    }

    public void generateInvariantAssertionStatement(ClassNode type, AnnotationNode annotation)  {

        // get the closure annotation
        classInvariant = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        BlockStatement assertionBlock = new BlockStatement();
        // assign the closure to a local variable and call() it
        VariableExpression closureVariable = new VariableExpression("$invariantClosure");

        // create a local variable to hold a reference to the newly instantiated closure
        assertionBlock.addStatement(new ExpressionStatement(
                new DeclarationExpression(closureVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        classInvariant)));

        assertionBlock.addStatement(new AssertStatement(new BooleanExpression(
                new MethodCallExpression(closureVariable, "call", ArgumentListExpression.EMPTY_ARGUMENTS)
        ), new ConstantExpression("[invariant]")));

        for (ConstructorNode constructor : type.getDeclaredConstructors())  {
            ((BlockStatement) constructor.getCode()).addStatement(assertionBlock);
        }
    }

    public void generateInvariantAssertionStatement(MethodNode method)  {

        BlockStatement invariantCheck = createAssertionExpression(method, classInvariant, "invariant");
        BlockStatement methodBlock = (BlockStatement) method.getCode();

        methodBlock.addStatement(invariantCheck);
    }

    public void generatePreconditionAssertionStatement(MethodNode method, AnnotationNode annotation)  {

        // get the closure annotation
        ClosureExpression closureExpression = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        BlockStatement preconditionCheck = createAssertionExpression(method, closureExpression, "precondition");
        preconditionCheck.addStatement(method.getCode());

        method.setCode(preconditionCheck);
    }

    public void generatePostconditionAssertionStatement(MethodNode method, AnnotationNode annotation)  {

        // get the closure annotation
        ClosureExpression closureExpression = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        BlockStatement methodBlock = (BlockStatement) method.getCode();

        // if return type is not void, than a "result" variable is provided in the postcondition expression
        List<Statement> statements = methodBlock.getStatements();
        if (statements.size() > 0)  {
            BlockStatement postconditionCheck = null;

            if (method.getReturnType() != ClassHelper.VOID_TYPE)  {
                Statement lastStatement = statements.get(statements.size() - 1);

                ReturnStatement returnStatement = getReturnStatement(lastStatement);

                // Assign the return statement expression to a local variable of type Object
                VariableExpression resultVariable = new VariableExpression("result");
                ExpressionStatement resultVariableStatement = new ExpressionStatement(
                new DeclarationExpression(resultVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        returnStatement.getExpression()));

                statements.remove(statements.size() - 1);

                postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition", new Parameter(ClassHelper.DYNAMIC_TYPE, "result"));
                postconditionCheck.getStatements().add(0, resultVariableStatement);

                methodBlock.addStatement(postconditionCheck);
                methodBlock.addStatement(returnStatement);
            } else {
                postconditionCheck = createAssertionExpression(method, closureExpression, "postcondition");
                // postconditionCheck.addStatement(returnStatement);
                methodBlock.addStatement(postconditionCheck);
            }
        }
    }

    private ReturnStatement getReturnStatement(Statement lastStatement)  {

        if (lastStatement instanceof ReturnStatement)  {
            return (ReturnStatement) lastStatement;
        } else {
            BlockStatement blockStatement = (BlockStatement) lastStatement;
            List<Statement> statements = blockStatement.getStatements();

            return statements.size() > 0 ? getReturnStatement(statements.get(statements.size() - 1)) : null;
        }
    }

    private BlockStatement createAssertionExpression(MethodNode method, ClosureExpression closureExpression, String constraint, Parameter... optionalParameters) {
        BlockStatement assertionBlock = new BlockStatement();
        // assign the closure to a local variable and call() it
        VariableExpression closureVariable = new VariableExpression("$" + constraint + "Closure");

        // create a local variable to hold a reference to the newly instantiated closure
        assertionBlock.addStatement(new ExpressionStatement(
                new DeclarationExpression(closureVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        closureExpression)));

        List<Expression> expressions = new ArrayList<Expression>();

        // add optional parameters first in argument list
        for (Parameter parameter : optionalParameters)  {
            expressions.add(new VariableExpression(parameter));
        }

        assertionBlock.addStatement(new AssertStatement(new BooleanExpression(
                new MethodCallExpression(closureVariable, "call", expressions.size() <= 1 ? new ArgumentListExpression(expressions) : new ArgumentListExpression(new ArrayExpression(ClassHelper.OBJECT_TYPE, expressions)))
        ), new ConstantExpression("[" + constraint + "] method " + method.getName() + "(" + getMethodParameters(method) + ")")));

        return assertionBlock;
    }

    private String getMethodParameters(MethodNode method)  {
        StringBuilder builder = new StringBuilder();

        for (Parameter parameter : method.getParameters())  {
            if (builder.length() > 0)  {
                builder.append(", ");
            }
            builder.append(parameter.getName()).append(":").append(parameter.getType().getTypeClass().getName());
        }

        return builder.toString();
    }
}
