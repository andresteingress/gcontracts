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
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.ClassInvariantViolation;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.ast.visitor.BaseVisitor;
import org.gcontracts.util.AnnotationUtils;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * Code generator for class invariants.
 * </p>
 *
 * @author ast
 */
public class ClassInvariantGenerator extends BaseGenerator {

    public ClassInvariantGenerator(final ReaderSource source) {
        super(source);
    }

    /**
     * Reads the {@link org.gcontracts.annotations.Invariant} boolean expression and generates a synthetic
     * method holding this class invariant. This is used for heir calls to find out about inherited class
     * invariants.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param classInvariant the {@link org.codehaus.groovy.ast.expr.BooleanExpression} containing the assertion expression
     * @param isDefaultInvariant specifies whether this is used to generate a default invariant
     */
    public void generateInvariantAssertionStatement(final ClassNode type, final BooleanExpression classInvariant, boolean isDefaultInvariant)  {

        BooleanExpression classInvariantExpression = addCallsToSuperClassInvariants(type, classInvariant);

        final BlockStatement assertBlockStatement = new BlockStatement();
        final AssertStatement invariantAssertionStatement = AssertStatementCreationUtility.getInvariantAssertionStatement(type, classInvariantExpression);
        if (isDefaultInvariant)  {
            // set a dummy message expression in order to avoid NP in Groovy 1.8 rc1
            invariantAssertionStatement.setMessageExpression(new ConstantExpression(""));
        }

        assertBlockStatement.addStatement(TryCatchBlockGenerator.generateTryCatchStatement(ClassHelper.makeWithoutCaching(ClassInvariantViolation.class), "<class invariant> " + type.getName() + "\n\n", invariantAssertionStatement));

        final BlockStatement blockStatement = new BlockStatement();
        blockStatement.addStatement(new IfStatement(new BooleanExpression(new VariableExpression(BaseVisitor.GCONTRACTS_ENABLED_VAR)), assertBlockStatement, new BlockStatement()));
        blockStatement.addStatement(new ReturnStatement(ConstantExpression.TRUE));

        // add a local protected method with the invariant closure - this is needed for invariant checks in inheritance lines
        type.addMethod(getInvariantMethodName(type), Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.Boolean_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, blockStatement);
    }

    /**
     * Modifies the given <tt>booleanExpression</tt> which contains that current class-invariant and adds a super-call the
     * the class-invariant of the next parent class which has the Invarian annotation.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param booleanExpression the current class-invariant as {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     *
     * @return the modified {@link BooleanExpression}
     */
    public BooleanExpression addCallsToSuperClassInvariants(final ClassNode type, final BooleanExpression booleanExpression)  {

        final ClassNode nextClassWithInvariant = AnnotationUtils.getClassNodeInHierarchyWithAnnotation(type.getSuperClass(), Invariant.class);
        if (nextClassWithInvariant == null) return booleanExpression;

        final String methodName = getInvariantMethodName(nextClassWithInvariant);

        return new BooleanExpression(
                 new BinaryExpression(
                         booleanExpression.getExpression(),
                         Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                         new BooleanExpression(new MethodCallExpression(VariableExpression.THIS_EXPRESSION, methodName, ArgumentListExpression.EMPTY_ARGUMENTS))));
    }

    /**
     * Adds the current class-invariant to the given <tt>method</tt>.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} which declared the given {@link org.codehaus.groovy.ast.MethodNode}
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     */
    public void addInvariantAssertionStatement(final ClassNode type, final MethodNode method)  {

        final String invariantMethodName = getInvariantMethodName(type);
        final MethodNode invariantMethod = type.getDeclaredMethod(invariantMethodName, Parameter.EMPTY_ARRAY);
        if (invariantMethod == null) return;

        final IfStatement invariantAssertionStatement = AssertStatementCreationUtility.getAssertStatementFromInvariantMethod(invariantMethod);

        final Statement statement = method.getCode();
        if (statement instanceof BlockStatement && method.getReturnType() != ClassHelper.VOID_TYPE && !(method instanceof ConstructorNode))  {
            final BlockStatement blockStatement = (BlockStatement) statement;

            final ReturnStatement returnStatement = AssertStatementCreationUtility.getReturnStatement(type, method, blockStatement);
            if (returnStatement != null)  {
                AssertStatementCreationUtility.removeReturnStatement(blockStatement, returnStatement);
                blockStatement.addStatement(invariantAssertionStatement);
                blockStatement.addStatement(returnStatement);
            } else {
                blockStatement.addStatement(invariantAssertionStatement);
            }

        } else if (statement instanceof BlockStatement) {
            final BlockStatement blockStatement = (BlockStatement) statement;
            blockStatement.addStatement(invariantAssertionStatement);
        } else {
            final BlockStatement assertionBlock = new BlockStatement();
            assertionBlock.addStatement(statement);
            assertionBlock.addStatement(invariantAssertionStatement);

            method.setCode(assertionBlock);
        }
    }
}
