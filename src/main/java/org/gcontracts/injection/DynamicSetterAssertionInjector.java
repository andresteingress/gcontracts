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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Assertion injector for injecting class invariants in Groovy generated setter methods.<p/>
 *
 * This AST transformation must be run after execution of {@link org.gcontracts.ast.ContractValidationASTTransformation}.
 *
 * @see org.gcontracts.ast.ContractValidationASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
public class DynamicSetterAssertionInjector extends Injector {

    private final SourceUnit sourceUnit;
    private final ReaderSource source;
    private final ClassNode classNode;

    public DynamicSetterAssertionInjector(final SourceUnit sourceUnit, final ReaderSource source, final ClassNode classNode)  {
        this.sourceUnit = sourceUnit;
        this.source = source;
        this.classNode = classNode;
    }

    public void rewrite()  {

        // if a class invariant is available visit all property nodes else skip this class
        final FieldNode invariantField = getInvariantClosureFieldNode(classNode);
        if (invariantField == null) return;

        final String closureSourceCode = convertClosureExpressionToSourceCode((ClosureExpression) invariantField.getInitialValueExpression(), source);

        new ClassCodeVisitorSupport()  {

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            protected Statement createSetterBlock(PropertyNode propertyNode, final FieldNode field, final Parameter parameter) {
                final BlockStatement setterMethodBlock = new BlockStatement();

                // check invariant before assignment

                setterMethodBlock.addStatement(AssertStatementCreator.getInvariantAssertionStatement(classNode, invariantField, closureSourceCode));

                // do assignment
                BinaryExpression fieldAssignment = new BinaryExpression(new FieldExpression(field), Token.newSymbol(Types.ASSIGN, -1, -1), new VariableExpression(parameter));
                setterMethodBlock.addStatement(new org.codehaus.groovy.ast.stmt.ExpressionStatement(fieldAssignment));


                // check invariant after assignment
                setterMethodBlock.addStatement(AssertStatementCreator.getInvariantAssertionStatement(classNode, invariantField, closureSourceCode));

                return setterMethodBlock;
            }

            public void visitProperty(PropertyNode node) {
                final String setterName = "set" + MetaClassHelper.capitalize(node.getName());

                final Statement setterBlock = node.getSetterBlock();
                final Parameter parameter = new Parameter(node.getType(), "value");

                if (isClassInvariantCandidate(node) && (setterBlock == null && classNode.getMethod(setterName, new Parameter[]{ parameter } ) == null)) {
                    final Statement setterBlockStatement = createSetterBlock(node, node.getField(), parameter);
                    node.setSetterBlock(setterBlockStatement);
                }
            }

            @Override
            public void visitClass(ClassNode classNode) {
                super.visitClass(classNode);

                List<ConstructorNode> declaredConstructors = classNode.getDeclaredConstructors();
                if (declaredConstructors == null || declaredConstructors.isEmpty())  {
                    // create default constructor with class invariant check
                    ConstructorNode constructor = new ConstructorNode(Opcodes.ACC_PUBLIC, AssertStatementCreator.getInvariantAssertionStatement(classNode, invariantField, closureSourceCode));
                    constructor.setSynthetic(true);
                    classNode.addConstructor(constructor);
                }
            }
            
        }.visitClass(classNode);
    }
}
