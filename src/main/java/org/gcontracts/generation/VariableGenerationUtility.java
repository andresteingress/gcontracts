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

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

/**
 * @author andre.steingress@gmail.com
 */
public class VariableGenerationUtility {

    public static final String OLD_VARIABLES_METHOD = "computeOldVariables";

    /**
     * Creates a synthetic method handling generation of the <tt>old</tt> variable map. If a super class declares
     * the same synthetic method it will be called and the results will be merged.
     *
     * @param classNode which contains postconditions, so an old variable generating method makes sense here.
     */
    public static void addOldVariableMethodNode(final ClassNode classNode)  {
        if (classNode.getDeclaredMethod(OLD_VARIABLES_METHOD, Parameter.EMPTY_ARRAY) != null) return;

        final BlockStatement methodBlockStatement = new BlockStatement();

        final MapExpression oldVariablesMap = new MapExpression();

        // create variable assignments for old variables
        for (final FieldNode fieldNode : classNode.getFields())   {
            if (fieldNode.getName().startsWith("$")) continue;

            final ClassNode fieldType = ClassHelper.getWrapper(fieldNode.getType());

            if (fieldType.getName().startsWith("java.lang") || ClassHelper.isPrimitiveType(fieldType) || fieldType.getName().startsWith("java.math") ||
                    fieldType.getName().startsWith("java.util") ||
                    fieldType.getName().startsWith("java.sql") ||
                    fieldType.getName().equals("groovy.lang.GString")  ||
                    fieldType.getName().equals("java.lang.String"))  {

                MethodNode cloneMethod = fieldType.getMethod("clone", Parameter.EMPTY_ARRAY);
                // if a clone classNode is available, the value is cloned
                if (cloneMethod != null && fieldType.implementsInterface(ClassHelper.make("java.lang.Cloneable")))  {
                    VariableExpression oldVariable = new VariableExpression("$old$" + fieldNode.getName());

                    final MethodCallExpression methodCall = new MethodCallExpression(new FieldExpression(fieldNode), "clone", ArgumentListExpression.EMPTY_ARGUMENTS);
                    // return null if field is null
                    methodCall.setSafe(true);

                    ExpressionStatement oldVariableAssignment = new ExpressionStatement(
                        new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                                methodCall));

                    methodBlockStatement.addStatement(oldVariableAssignment);
                    oldVariablesMap.addMapEntryExpression(new MapEntryExpression(new ConstantExpression(oldVariable.getName().substring("$old$".length())), oldVariable));

                } else if (ClassHelper.isPrimitiveType(fieldType)
                        || ClassHelper.isNumberType(fieldType)
                        || fieldType.getName().startsWith("java.math")
                        || fieldType.getName().equals("groovy.lang.GString")
                        || fieldType.getName().equals("java.lang.String")) {

                    VariableExpression oldVariable = new VariableExpression("$old$" + fieldNode.getName());
                    ExpressionStatement oldVariableAssignment = new ExpressionStatement(
                        new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new FieldExpression(fieldNode)));

                    methodBlockStatement.addStatement(oldVariableAssignment);
                    oldVariablesMap.addMapEntryExpression(new MapEntryExpression(new ConstantExpression(oldVariable.getName().substring("$old$".length())), oldVariable));
                }
            }
        }

        VariableExpression oldVariable = new VariableExpression("old", ClassHelper.MAP_TYPE);
        ExpressionStatement oldVariabeStatement = new ExpressionStatement(
                new DeclarationExpression(oldVariable,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        oldVariablesMap));

        methodBlockStatement.addStatement(oldVariabeStatement);

        VariableExpression mergedOldVariables = null;

        // let's ask the super class for old variables...
        if (classNode.getSuperClass() != null && classNode.getSuperClass().getMethod(OLD_VARIABLES_METHOD, Parameter.EMPTY_ARRAY) != null)  {
            mergedOldVariables = new VariableExpression("mergedOldVariables", ClassHelper.MAP_TYPE);
            ExpressionStatement mergedOldVariablesStatement = new ExpressionStatement(
                new DeclarationExpression(mergedOldVariables,
                        Token.newSymbol(Types.ASSIGN, -1, -1),
                        new MethodCallExpression(oldVariable, "plus", new ArgumentListExpression(new MethodCallExpression(VariableExpression.SUPER_EXPRESSION, OLD_VARIABLES_METHOD, ArgumentListExpression.EMPTY_ARGUMENTS)))));


            methodBlockStatement.addStatement(mergedOldVariablesStatement);
        }

        methodBlockStatement.addStatement(new ReturnStatement(mergedOldVariables != null ? mergedOldVariables : oldVariable));

        final MethodNode preconditionMethodNode = classNode.addMethod(OLD_VARIABLES_METHOD, Opcodes.ACC_PROTECTED, ClassHelper.DYNAMIC_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, methodBlockStatement);
        preconditionMethodNode.setSynthetic(true);

    }
}
