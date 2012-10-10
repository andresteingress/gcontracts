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
package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.ClassInvariantViolation;
import org.gcontracts.PostconditionViolation;
import org.gcontracts.PreconditionViolation;
import org.gcontracts.annotations.meta.ContractElement;
import org.gcontracts.annotations.meta.Postcondition;
import org.gcontracts.classgen.asm.ContractClosureWriter;
import org.gcontracts.generation.AssertStatementCreationUtility;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.TryCatchBlockGenerator;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.ExpressionUtils;
import org.gcontracts.util.FieldValues;
import org.gcontracts.util.Validate;
import org.objectweb.asm.Opcodes;

import java.util.*;

/**
 * Visits interfaces &amp; classes and looks for <tt>@Requires</tt> or <tt>@Ensures</tt> and creates {@link groovy.lang.Closure}
 * classes for the annotation closures.<p/>
 *
 * The annotation closure classes are used later on to check interface contract pre- and post-conditions in
 * implementation classes.
 *
 * @see org.gcontracts.annotations.Requires
 * @see org.gcontracts.annotations.Ensures
 *
 * @see org.gcontracts.ast.visitor.BaseVisitor
 *
 * @author ast
 */
public class AnnotationClosureVisitor extends BaseVisitor implements ASTNodeMetaData{

    public static final String META_DATA_USE_EXECUTION_TRACKER = "org.gcontracts.META_DATA.USE_EXECUTION_TRACKER";
    public static final String META_DATA_ORIGINAL_TRY_CATCH_BLOCK = "org.gcontracts.META_DATA.ORIGINAL_TRY_CATCH_BLOCK";

    private static final String POSTCONDITION_TYPE_NAME = Postcondition.class.getName();
    private static final ClassNode FIELD_VALUES = ClassHelper.makeCached(FieldValues.class);

    private ClassNode classNode;
    private final ContractClosureWriter contractClosureWriter = new ContractClosureWriter();

    public AnnotationClosureVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node == null) return;
        if ( !(CandidateChecks.isInterfaceContractsCandidate(node) || CandidateChecks.isContractsCandidate(node)) ) return;

        classNode = node;

        if (classNode.getNodeMetaData(PROCESSED) == null  && CandidateChecks.isContractsCandidate(node))  {
            final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(node, ContractElement.class.getName());
            for (AnnotationNode annotationNode : annotationNodes)  {
                Expression expression = annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);
                if (expression == null || expression instanceof ClassExpression) continue;

                ClosureExpression closureExpression = (ClosureExpression) expression;

                ClosureExpressionValidator validator = new ClosureExpressionValidator(classNode, null, annotationNode, sourceUnit);
                validator.visitClosureExpression(closureExpression);
                validator.secondPass(closureExpression);

                List<Parameter> parameters = new ArrayList<Parameter>(Arrays.asList(closureExpression.getParameters()));

                final List<BooleanExpression> booleanExpressions = ExpressionUtils.getBooleanExpression(closureExpression);
                if (booleanExpressions == null || booleanExpressions.isEmpty()) continue;

                BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();

                BlockStatement newClosureBlockStatement = TryCatchBlockGenerator.generateTryCatchBlock(
                        ClassHelper.makeWithoutCaching(ClassInvariantViolation.class),
                        "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + " \n\n",
                        AssertStatementCreationUtility.getAssertionStatemens(booleanExpressions)
                );

                newClosureBlockStatement.setSourcePosition(closureBlockStatement);

                ClosureExpression rewrittenClosureExpression = new ClosureExpression(parameters.toArray(new Parameter[parameters.size()]), newClosureBlockStatement);
                rewrittenClosureExpression.setSourcePosition(closureExpression);
                rewrittenClosureExpression.setDeclaringClass(closureExpression.getDeclaringClass());
                rewrittenClosureExpression.setSynthetic(true);
                rewrittenClosureExpression.setVariableScope(closureExpression.getVariableScope());
                rewrittenClosureExpression.setType(closureExpression.getType());

                ClassNode closureClassNode = contractClosureWriter.createClosureClass(classNode, null, rewrittenClosureExpression, false, false, Opcodes.ACC_PUBLIC);
                classNode.getModule().addClass(closureClassNode);

                final ClassExpression value = new ClassExpression(closureClassNode);
                value.setSourcePosition(annotationNode);

                BlockStatement value1 = TryCatchBlockGenerator.generateTryCatchBlockForInlineMode(
                        ClassHelper.makeWithoutCaching(ClassInvariantViolation.class),
                        "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + " \n\n",
                        AssertStatementCreationUtility.getAssertionStatemens(booleanExpressions)
                );
                value1.setNodeMetaData(META_DATA_USE_EXECUTION_TRACKER, validator.isMethodCalls());

                value.setNodeMetaData(META_DATA_ORIGINAL_TRY_CATCH_BLOCK, value1);

                annotationNode.setMember(CLOSURE_ATTRIBUTE_NAME, value);

                markClosureReplaced(classNode);
            }
        }

        super.visitClass(node);

        // generate closure classes for the super class and all implemented interfaces
        visitClass(node.getSuperClass());
        for (ClassNode i : node.getInterfaces())  {
            visitClass(i);
        }

        markProcessed(classNode);
    }

    @Override
    public void visitConstructorOrMethod(MethodNode methodNode, boolean isConstructor) {
        if (!CandidateChecks.couldBeContractElementMethodNode(classNode, methodNode) && !(CandidateChecks.isPreconditionCandidate(classNode, methodNode))) return;
        if (methodNode.getNodeMetaData(PROCESSED) != null) return;

        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName());
        for (AnnotationNode annotationNode : annotationNodes)  {
            replaceWithClosureClassReference(annotationNode, methodNode);
        }

        markProcessed(methodNode);

        super.visitConstructorOrMethod(methodNode, isConstructor);
    }

    private void replaceWithClosureClassReference(AnnotationNode annotationNode, MethodNode methodNode) {
        Validate.notNull(annotationNode);
        Validate.notNull(methodNode);

        // check whether this is a pre- or postcondition
        boolean isPostcondition = AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), org.gcontracts.annotations.meta.Postcondition.class.getName());

        Expression expression = annotationNode.getMember(CLOSURE_ATTRIBUTE_NAME);
        if (expression == null || expression instanceof ClassExpression) return;

        ClosureExpression closureExpression = (ClosureExpression) expression;

        ClosureExpressionValidator validator = new ClosureExpressionValidator(classNode, methodNode, annotationNode, sourceUnit);
        validator.visitClosureExpression(closureExpression);
        validator.secondPass(closureExpression);

        List<Parameter> parameters = new ArrayList<Parameter>(Arrays.asList(closureExpression.getParameters()));

        parameters.addAll(new ArrayList<Parameter>(Arrays.asList(methodNode.getParameters())));

        final List<BooleanExpression> booleanExpressions = ExpressionUtils.getBooleanExpression(closureExpression);
        if (booleanExpressions == null || booleanExpressions.isEmpty()) return;

        BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();

        BlockStatement newClosureBlockStatement = TryCatchBlockGenerator.generateTryCatchBlock(
                isPostcondition ? ClassHelper.makeWithoutCaching(PostconditionViolation.class) : ClassHelper.makeWithoutCaching(PreconditionViolation.class),
                "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + "." + methodNode.getTypeDescriptor() + " \n\n",
                AssertStatementCreationUtility.getAssertionStatemens(booleanExpressions)
        );

        newClosureBlockStatement.setSourcePosition(closureBlockStatement);

        ClosureExpression rewrittenClosureExpression = new ClosureExpression(parameters.toArray(new Parameter[parameters.size()]), newClosureBlockStatement);
        rewrittenClosureExpression.setSourcePosition(closureExpression);
        rewrittenClosureExpression.setDeclaringClass(closureExpression.getDeclaringClass());
        rewrittenClosureExpression.setSynthetic(true);
        rewrittenClosureExpression.setVariableScope(correctVariableScope(closureExpression.getVariableScope(), methodNode));
        rewrittenClosureExpression.setType(closureExpression.getType());

        boolean isConstructor = methodNode instanceof ConstructorNode;
        ClassNode closureClassNode = contractClosureWriter.createClosureClass(classNode, methodNode, rewrittenClosureExpression, isPostcondition && !isConstructor, isPostcondition && !isConstructor, Opcodes.ACC_PUBLIC);
        classNode.getModule().addClass(closureClassNode);

        final ClassExpression value = new ClassExpression(closureClassNode);
        value.setSourcePosition(annotationNode);

        BlockStatement value1 = TryCatchBlockGenerator.generateTryCatchBlockForInlineMode(
                isPostcondition ? ClassHelper.makeWithoutCaching(PostconditionViolation.class) : ClassHelper.makeWithoutCaching(PreconditionViolation.class),
                "<" + annotationNode.getClassNode().getName() + "> " + classNode.getName() + "." + methodNode.getTypeDescriptor() + " \n\n",
                AssertStatementCreationUtility.getAssertionStatemens(booleanExpressions)
        );
        value1.setNodeMetaData(META_DATA_USE_EXECUTION_TRACKER, validator.isMethodCalls());

        value.setNodeMetaData(META_DATA_ORIGINAL_TRY_CATCH_BLOCK, value1);
        annotationNode.setMember(CLOSURE_ATTRIBUTE_NAME, value);

        markClosureReplaced(methodNode);
    }

    private VariableScope correctVariableScope(VariableScope variableScope, MethodNode methodNode) {
        if (variableScope ==  null) return null;
        if (methodNode == null || methodNode.getParameters() == null || methodNode.getParameters().length == 0) return variableScope;

        VariableScope copy = copy(variableScope);

        for (Iterator<Variable> iterator = variableScope.getReferencedClassVariablesIterator(); iterator.hasNext();) {
            Variable variable = iterator.next();
            String name = variable.getName();

            for (Parameter parameter : methodNode.getParameters())  {
                if (parameter.getName().equals(name))  {
                    copy.putReferencedLocalVariable(parameter);
                    break;
                }
            }

            if (!copy.isReferencedLocalVariable(name))  {
                copy.putReferencedClassVariable(variable);
            }
        }

        return copy;
    }

    private VariableScope copy(VariableScope original) {
        VariableScope copy = new VariableScope(original.getParent());
        copy.setClassScope(original.getClassScope());
        copy.setInStaticContext(original.isInStaticContext());
        return copy;
    }

    private void markProcessed(ASTNode someNode) {
        if (someNode.getNodeMetaData(PROCESSED) == null)
            someNode.setNodeMetaData(PROCESSED, Boolean.TRUE);
    }

    private void markClosureReplaced(ASTNode someNode) {
        if (someNode.getNodeMetaData(CLOSURE_REPLACED) == null)
            someNode.setNodeMetaData(CLOSURE_REPLACED, Boolean.TRUE);
    }

    static class ClosureExpressionValidator extends ClassCodeVisitorSupport implements Opcodes {

        private final ClassNode classNode;
        private final MethodNode methodNode;
        private final AnnotationNode annotationNode;
        private final SourceUnit sourceUnit;

        private final Map<VariableExpression, StaticMethodCallExpression> variableExpressions;

        private boolean secondPass = false;
        private boolean methodCalls = false;

        public ClosureExpressionValidator(ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode, SourceUnit sourceUnit)  {
            this.classNode = classNode;
            this.methodNode = methodNode;
            this.annotationNode = annotationNode;
            this.sourceUnit = sourceUnit;
            this.variableExpressions = new HashMap<VariableExpression, StaticMethodCallExpression> ();
        }

        @Override
        public void visitClosureExpression(ClosureExpression expression) {
            secondPass = false;

            if (expression.getCode() == null || expression.getCode() instanceof EmptyStatement)  {
                addError("[GContracts] Annotation does not contain any expressions (e.g. use '@Requires({ argument1 })').", expression);
            }

            if (expression.getCode() instanceof BlockStatement &&
                    ((BlockStatement) expression.getCode()).getStatements().isEmpty())  {
                addError("[GContracts] Annotation does not contain any expressions (e.g. use '@Requires({ argument1 })').", expression);
            }

            if (expression.isParameterSpecified() && !AnnotationUtils.hasAnnotationOfType(annotationNode.getClassNode(), POSTCONDITION_TYPE_NAME))  {
                addError("[GContracts] Annotation does not support parameters (the only exception are postconditions).", expression);
            }

            if (expression.isParameterSpecified())  {
                for (Parameter param : expression.getParameters())  {
                    if (!("result".equals(param.getName()) || "old".equals(param.getName())))  {
                        addError("[GContracts] Postconditions only allow 'old' and 'result' closure parameters.", expression);
                    }

                    if (!param.isDynamicTyped())  {
                        addError("[GContracts] Postconditions do not support explicit types.", expression);
                    }
                }
            }

            super.visitClosureExpression(expression);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {

            // in case of a FieldNode, checks whether the FieldNode can be replaced with a Parameter
            Variable accessedVariable = getParameterCandidate(expression.getAccessedVariable());
            if (accessedVariable instanceof FieldNode)  {
                FieldNode fieldNode = (FieldNode) accessedVariable;

                if ((fieldNode.getModifiers() & ACC_PRIVATE) != 0 && !classNode.hasProperty(fieldNode.getName()))  {
                    // if this is a class invariant we'll change the field node access
                    StaticMethodCallExpression staticMethodCallExpression = new StaticMethodCallExpression(FIELD_VALUES, "fieldValue", new ArgumentListExpression(VariableExpression.THIS_EXPRESSION, new ConstantExpression(fieldNode.getName()), new ClassExpression(fieldNode.getType())));
                    variableExpressions.put(expression, staticMethodCallExpression);
                }
            }

            if (accessedVariable instanceof Parameter)  {
                Parameter parameter = (Parameter) accessedVariable;
                if ("it".equals(parameter.getName()))  {
                    addError("[GContracts] Access to 'it' is not supported.", expression);
                }
            }

            expression.setAccessedVariable(accessedVariable);

            super.visitVariableExpression(expression);
        }

        @Override
        public void visitPostfixExpression(PostfixExpression expression) {
            checkOperation(expression, expression.getOperation());

            if (secondPass)  {
                if (expression.getExpression() instanceof VariableExpression)  {
                    VariableExpression variableExpression = (VariableExpression) expression.getExpression();
                    if (variableExpressions.containsKey(variableExpression))  {
                        expression.setExpression(variableExpressions.get(variableExpression));
                    }
                }
            }

            super.visitPostfixExpression(expression);
        }

        @Override
        public void visitPrefixExpression(PrefixExpression expression) {
            checkOperation(expression, expression.getOperation());

            if (secondPass)  {
                if (expression.getExpression() instanceof VariableExpression)  {
                    VariableExpression variableExpression = (VariableExpression) expression.getExpression();
                    if (variableExpressions.containsKey(variableExpression))  {
                        expression.setExpression(variableExpressions.get(variableExpression));
                    }
                }
            }

            super.visitPrefixExpression(expression);
        }

        @Override
        public void visitBinaryExpression(BinaryExpression expression) {
            checkOperation(expression, expression.getOperation());

            if (secondPass)  {
                if (expression.getLeftExpression() instanceof VariableExpression)  {
                    VariableExpression variableExpression = (VariableExpression) expression.getLeftExpression();
                    if (variableExpressions.containsKey(variableExpression))  {
                        expression.setLeftExpression(variableExpressions.get(variableExpression));
                    }
                }
                if (expression.getRightExpression() instanceof VariableExpression)  {
                    VariableExpression variableExpression = (VariableExpression) expression.getRightExpression();
                    if (variableExpressions.containsKey(variableExpression))  {
                        expression.setRightExpression(variableExpressions.get(variableExpression));
                    }
                }
            }

            super.visitBinaryExpression(expression);
        }

        @Override
        public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
            methodCalls = true;
            super.visitStaticMethodCallExpression(call);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            methodCalls = true;
            super.visitMethodCallExpression(call);
        }

        @Override
        public void visitConstructorCallExpression(ConstructorCallExpression call) {
            methodCalls = true;
            super.visitConstructorCallExpression(call);
        }

        private void checkOperation(Expression expression, Token operation) {
            if (Types.ofType(operation.getType(), Types.ASSIGNMENT_OPERATOR))  {
                addError("[GContracts] Assignment operators are not supported.", expression);
            }
            if (Types.ofType(operation.getType(), Types.POSTFIX_OPERATOR))  {
                addError("[GContracts] State changing postfix & prefix operators are not supported.", expression);
            }
        }

        private Variable getParameterCandidate(Variable variable)  {
            if (variable == null || methodNode == null) return variable;
            if (variable instanceof Parameter) return variable;

            String name = variable.getName();
            for (Parameter param : methodNode.getParameters())  {
                if (name.equals(param.getName())) return param;
            }

            return variable;
        }

        public void secondPass(ClosureExpression closureExpression)  {
            secondPass = true;
            super.visitClosureExpression(closureExpression);
        }

        public boolean isMethodCalls() {
            return methodCalls;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return sourceUnit;
        }
    }

}