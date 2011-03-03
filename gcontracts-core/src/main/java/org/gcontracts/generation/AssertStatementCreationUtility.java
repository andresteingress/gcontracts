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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.stmt.*;

import java.util.List;

/**
 * Central place to create {@link org.codehaus.groovy.ast.stmt.AssertStatement} instances in GContracts. Utilized
 * to centralize {@link AssertionError} message generation.
 *
 * @see org.codehaus.groovy.ast.stmt.AssertStatement
 * @see AssertionError
 *
 * @author ast
 */
public final class AssertStatementCreationUtility {

    /**
     * Reusable method for creating assert statements for the given <tt>booleanExpression</tt>.
     *
     * @param booleanExpression the assertion's {@link org.codehaus.groovy.ast.expr.BooleanExpression}
     *
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static AssertStatement getAssertionStatement(final BooleanExpression booleanExpression)  {

        final AssertStatement assertStatement = new AssertStatement(booleanExpression);
        assertStatement.setSourcePosition(booleanExpression);

        return assertStatement;
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

    /**
     * Removes a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} from the given {@link org.codehaus.groovy.ast.stmt.Statement}.
     */
    public static void removeReturnStatement(BlockStatement statement, ReturnStatement returnStatement)  {

        List<Statement> statements = statement.getStatements();
        for (int i = statements.size() - 1; i >= 0; i--)  {
            Statement stmt = statements.get(i);
            if (stmt == returnStatement) {
                statements.remove(i);
                return;
            } else if (stmt instanceof BlockStatement)  {
                removeReturnStatement((BlockStatement) stmt, returnStatement);
                return;
            }
        }
    }
}
