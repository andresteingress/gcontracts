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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.visitors.ClassInvariantVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Injects standard Java assertion statements which check the specified {@link org.gcontracts.annotations.Requires},
 * {@link org.gcontracts.annotations.Ensures} and {@link org.gcontracts.annotations.Invariant} closure annotations.
 *
 * @see org.gcontracts.annotations.Requires
 * @see org.gcontracts.annotations.Ensures
 * @see org.gcontracts.annotations.Invariant
 */
public class BasicAssertionInjector extends Injector {

    private static final String CLOSURE_ATTRIBUTE_NAME = "value";

    private final SourceUnit sourceUnit;
    private final ReaderSource source;
    private final ClassNode classNode;
    
    private ClosureExpression classInvariant;

    public BasicAssertionInjector(final SourceUnit sourceUnit, final ReaderSource source, final ClassNode classNode) {
        this.sourceUnit = sourceUnit;
        this.source = source;
        this.classNode = classNode;
    }

    /**
     * Rewrites the current {@link org.codehaus.groovy.ast.ClassNode} and adds assertions for all supported
     * assertion types.
     */
    public void rewrite() {
        new ClassCodeVisitorSupport() {

            @Override
            public void visitClass(ClassNode type) {

                final ClassInvariantVisitor classInvariantVisitor = new ClassInvariantVisitor(source);

                boolean found = false;

                List<AnnotationNode> annotations = type.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getName().equals(Invariant.class.getName()))  {
                        classInvariant = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);

                        classInvariantVisitor.generateInvariantAssertionStatement(type, classInvariant);

                        // fix compilation with setting value() to java.lang.Object.class
                        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

                        found = true;
                    }
                }

                if (!found)  {
                    classInvariant = classInvariantVisitor.generateDefaultInvariantAssertionStatement(type);
                }

                super.visitClass(type);
            }

            @Override
            public void visitMethod(MethodNode method) {

                super.visitMethod(method);

                final ClassInvariantVisitor classInvariantVisitor = new ClassInvariantVisitor(source);

                List<AnnotationNode> annotations = method.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getName().equals(Requires.class.getName()))  {
                        generatePreconditionAssertionStatement(method, annotation);
                    } else if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                        generatePostconditionAssertionStatement(method, annotation);
                    }
                }

                // If there is a class invariant we will append the check to this invariant
                // after each method call
                if (CandidateChecks.isClassInvariantCandidate(method))  {
                    classInvariantVisitor.generateInvariantAssertionStatement(method, classInvariant);
                }
            }

            protected SourceUnit getSourceUnit() {
                return null;
            }
        }.visitClass(classNode);
    }

    /**
     * Injects a precondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link org.gcontracts.annotations.Requires}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param annotation the {@link org.gcontracts.annotations.Requires} annotation
     */
    public void generatePreconditionAssertionStatement(MethodNode method, AnnotationNode annotation)  {

        // get the closure annotation
        final ClosureExpression closureExpression = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

        final BlockStatement preconditionCheck = AssertStatementCreator.getAssertionBlockStatement(method, closureExpression, "precondition", closureToSourceConverter.convertClosureExpressionToSourceCode(closureExpression, source));
        preconditionCheck.addStatement(method.getCode());

        method.setCode(preconditionCheck);
    }

    /**
     * Injects a postcondition assertion statement in the given <tt>method</tt>, based on the given <tt>annotation</tt> of
     * type {@link org.gcontracts.annotations.Ensures}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} for assertion injection
     * @param annotation the {@link org.gcontracts.annotations.Ensures} annotation
     */
    public void generatePostconditionAssertionStatement(MethodNode method, AnnotationNode annotation)  {

        // get the closure annotation
        final ClosureExpression closureExpression = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);
        // fix compilation with setting value() to java.lang.Object.class
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

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
