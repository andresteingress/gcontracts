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
package org.gcontracts.ast;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;

/**
 * @author andre.steingress@gmail.com
 */
public class VariableGenerator {

    /**
     * Each field of the class is assigned to an old variable before method execution. After method execution
     * a map with the old variable values is generated, which is than used as parameter in the {@link org.gcontracts.annotations.Ensures}
     * closure.
     *
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @return a {@link org.codehaus.groovy.ast.expr.MapExpression} which holds either none or some old variables
     */
    public MapExpression generateOldVariablesMap(MethodNode method) {

        final ClassNode declaringClass = method.getDeclaringClass();
        final ArrayList<ExpressionStatement> oldVariableAssignments = new ArrayList<ExpressionStatement>();
        final ArrayList<VariableExpression> oldVariableExpressions = new ArrayList<VariableExpression>();

        // create variable assignments for old variables
        for (final FieldNode fieldNode : declaringClass.getFields())   {
            final ClassNode fieldType = fieldNode.getType();

            if (fieldType.getName().startsWith("java.lang") || ClassHelper.isPrimitiveType(fieldType) || fieldType.getName().startsWith("java.math") ||
                    fieldType.getName().startsWith("java.util") || fieldType.getName().startsWith("java.sql"))  {

                MethodNode cloneMethod = fieldType.getMethod("clone", Parameter.EMPTY_ARRAY);
                // if a clone method is available, the value is cloned
                if (cloneMethod != null && fieldType.implementsInterface(ClassHelper.make("java.lang.Cloneable")))  {
                    VariableExpression oldVariable = new VariableExpression("$old$" + fieldNode.getName());

                    final MethodCallExpression methodCall = new MethodCallExpression(new FieldExpression(fieldNode), "clone", ArgumentListExpression.EMPTY_ARGUMENTS);
                    // return null if field is null
                    methodCall.setSafe(true);

                    ExpressionStatement oldVariableAssignment = new ExpressionStatement(
                        new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                                methodCall));

                    oldVariableExpressions.add(oldVariable);
                    oldVariableAssignments.add(oldVariableAssignment);

                } else if (ClassHelper.isPrimitiveType(fieldType) || fieldType.getName().startsWith("java.lang")) {
                    VariableExpression oldVariable = new VariableExpression("$old$" + fieldNode.getName());
                    ExpressionStatement oldVariableAssignment = new ExpressionStatement(
                        new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new FieldExpression(fieldNode)));

                    oldVariableExpressions.add(oldVariable);
                    oldVariableAssignments.add(oldVariableAssignment);
                }
            }
        }

        BlockStatement methodBlock = (BlockStatement) method.getCode();

        MapExpression oldVariablesMap = new MapExpression();
        // add old variable statements
        for (int i = 0; i < oldVariableAssignments.size(); i++)  {
            VariableExpression variable = oldVariableExpressions.get(i);

            methodBlock.getStatements().add(0, oldVariableAssignments.get(i));
            oldVariablesMap.addMapEntryExpression(new MapEntryExpression(new ConstantExpression(variable.getName().substring("$old$".length())), variable));
        }

        return oldVariablesMap;
    }
}
