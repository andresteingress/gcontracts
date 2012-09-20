/**
 * Copyright (c) 2011, Andre Steingress
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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.ViolationTracker;
import org.gcontracts.ast.visitor.BaseVisitor;
import org.gcontracts.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <pe
 * Base class for GContracts code generators.
 * </p>
 *
 * @author ast
 */
public abstract class BaseGenerator {

    public static final String INVARIANT_CLOSURE_PREFIX = "invariant";

    protected final ReaderSource source;

    public BaseGenerator(final ReaderSource source)  {
        this.source = source;
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the field name of the invariant closure field of the given <tt>classNode</tt>
     */
    public static String getInvariantMethodName(final ClassNode classNode)  {
        return INVARIANT_CLOSURE_PREFIX + "_" + classNode.getName().replaceAll("\\.", "_");
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the {@link org.codehaus.groovy.ast.MethodNode} which contains the invariant of the given <tt>classNode</tt>
     */
    public static MethodNode getInvariantMethodNode(final ClassNode classNode)  {
        return classNode.getDeclaredMethod(getInvariantMethodName(classNode), Parameter.EMPTY_ARRAY);
    }

    protected BlockStatement wrapAssertionBooleanExpression(ClassNode type, MethodNode methodNode, BooleanExpression classInvariantExpression, String assertionType) {

        final ClassNode violationTrackerClassNode = ClassHelper.makeWithoutCaching(ViolationTracker.class);
        final VariableExpression $_gc_result = new VariableExpression("$_gc_result", ClassHelper.boolean_TYPE);
        $_gc_result.setAccessedVariable($_gc_result);

        final BlockStatement assertBlockStatement = new BlockStatement();
        final TryCatchStatement lockTryCatchStatement = new TryCatchStatement(assertBlockStatement, new BlockStatement(Arrays.<Statement>asList(
                new ExpressionStatement(new MethodCallExpression(new ClassExpression(ClassHelper.make(ContractExecutionTracker.class)), "clear", new ArgumentListExpression(Arrays.<Expression>asList(new ConstantExpression(type.getName()), new ConstantExpression(methodNode.getTypeDescriptor()), new ConstantExpression(assertionType), methodNode.isStatic() ? ConstantExpression.TRUE : ConstantExpression.FALSE))))
        ), new VariableScope()));
        final BlockStatement ifBlockStatement = new BlockStatement();

        assertBlockStatement.addStatement(new IfStatement(new BooleanExpression(
                new MethodCallExpression(new ClassExpression(ClassHelper.make(ContractExecutionTracker.class)), "track", new ArgumentListExpression(Arrays.<Expression>asList(new ConstantExpression(type.getName()), new ConstantExpression(methodNode.getTypeDescriptor()), new ConstantExpression(assertionType), methodNode.isStatic() ? ConstantExpression.TRUE : ConstantExpression.FALSE)))),
                ifBlockStatement,
                EmptyStatement.INSTANCE
        ));


        ifBlockStatement.addStatement(new ExpressionStatement(new DeclarationExpression($_gc_result, Token.newSymbol(Types.ASSIGN, -1, -1), ConstantExpression.FALSE)));
        ifBlockStatement.addStatement(new ExpressionStatement(
               new MethodCallExpression(new ClassExpression(violationTrackerClassNode), "init", ArgumentListExpression.EMPTY_ARGUMENTS))
        );

        ifBlockStatement.addStatement(new TryCatchStatement(
              new ExpressionStatement(new BinaryExpression($_gc_result,
                Token.newSymbol(Types.ASSIGN, -1, -1),
                classInvariantExpression
              )),
              EmptyStatement.INSTANCE
        ));

        BlockStatement finallyBlockStatement = new BlockStatement();
        finallyBlockStatement.addStatement(new ExpressionStatement(new MethodCallExpression(new ClassExpression(violationTrackerClassNode), "deinit", ArgumentListExpression.EMPTY_ARGUMENTS)));

        ifBlockStatement.addStatement(
                new IfStatement(
                        new BooleanExpression(
                                new BinaryExpression(
                                        new NotExpression($_gc_result),
                                        Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                                        new MethodCallExpression(new ClassExpression(violationTrackerClassNode), "violationsOccured", ArgumentListExpression.EMPTY_ARGUMENTS)
                                )
                        ),
                        new TryCatchStatement(
                                new ExpressionStatement(new MethodCallExpression(new ClassExpression(violationTrackerClassNode), "rethrowFirst", ArgumentListExpression.EMPTY_ARGUMENTS)),
                                finallyBlockStatement
                        ),
                        new BlockStatement()
                )
        );

        final BlockStatement blockStatement = new BlockStatement();
        blockStatement.addStatement(new IfStatement(new BooleanExpression(new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR, ClassHelper.Boolean_TYPE)), lockTryCatchStatement, new BlockStatement()));

        return blockStatement;
    }

    // TODO: what about constructor method nodes - does it find a constructor node in the super class?
    protected BooleanExpression addCallsToSuperMethodNodeAnnotationClosure(final ClassNode type, final MethodNode methodNode, final Class<? extends Annotation> annotationType, BooleanExpression booleanExpression, boolean isPostcondition)  {

        final List<AnnotationNode> nextContractElementAnnotations = AnnotationUtils.getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), methodNode, ClassHelper.makeWithoutCaching(annotationType));
        if (nextContractElementAnnotations.isEmpty()) return booleanExpression;

        for (AnnotationNode nextContractElementAnnotation : nextContractElementAnnotations)  {
            ClassExpression classExpression = (ClassExpression) nextContractElementAnnotation.getMember(BaseVisitor.CLOSURE_ATTRIBUTE_NAME);
            if (classExpression == null) continue;

            ArgumentListExpression closureConstructorArgumentList = new ArgumentListExpression(
                    VariableExpression.THIS_EXPRESSION,
                    VariableExpression.THIS_EXPRESSION);


            ArgumentListExpression callArgumentList = new ArgumentListExpression();
            for (Parameter parameter : methodNode.getParameters())  {
                callArgumentList.addExpression(new VariableExpression(parameter));
            }

            if (isPostcondition && methodNode.getReturnType() != ClassHelper.VOID_TYPE && !(methodNode instanceof ConstructorNode))  {
                VariableExpression variableExpression = new VariableExpression("result", methodNode.getReturnType());
                variableExpression.setAccessedVariable(variableExpression);

                callArgumentList.addExpression(variableExpression);
            }

            if (isPostcondition && !(methodNode instanceof ConstructorNode)) {
                VariableExpression variableExpression = new VariableExpression("old", new ClassNode(Map.class));
                variableExpression.setAccessedVariable(variableExpression);

                callArgumentList.addExpression(variableExpression);
            }

            ArgumentListExpression newInstanceArguments = new ArgumentListExpression(
                    classExpression,
                    new ArrayExpression(
                            ClassHelper.DYNAMIC_TYPE,
                            Arrays.<Expression>asList(VariableExpression.THIS_EXPRESSION, VariableExpression.THIS_EXPRESSION)
                    )
            );

            StaticMethodCallExpression methodCallExpression = new StaticMethodCallExpression(
                    ClassHelper.makeWithoutCaching(InvokerHelper.class),
                    "newInstance",
                    newInstanceArguments
            );

            MethodCallExpression doCall = new MethodCallExpression(
                    methodCallExpression,
                    "call",
                    callArgumentList
            );

            final BooleanExpression rightExpression = new BooleanExpression(doCall);
            booleanExpression.setSourcePosition(nextContractElementAnnotation);

            booleanExpression = new BooleanExpression(
                    new BinaryExpression(
                            booleanExpression,
                            isPostcondition ? Token.newSymbol(Types.LOGICAL_AND, -1, -1) : Token.newSymbol(Types.LOGICAL_OR, -1, -1),
                            rightExpression)
            );
        }

        return booleanExpression;
    }
}
