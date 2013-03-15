/**
 * Copyright (c) 2013, Andre Steingress
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
package org.gcontracts.util;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>Internal utility class for extracting a boolean expression from the given expression or statement.</p>
 *
 * @see ClosureExpression
 * @see BooleanExpression
 *
 * @author ast
 */
public class ExpressionUtils {

    /**
     * Returns all {@link BooleanExpression} instances found in the given {@link ClosureExpression}.
     */
    public static List<BooleanExpression> getBooleanExpression(ClosureExpression closureExpression)  {
        if (closureExpression == null) return null;

        final BlockStatement closureBlockStatement = (BlockStatement) closureExpression.getCode();
        return getBooleanExpressions(closureBlockStatement);
    }

    /**
     * Returns all {@link BooleanExpression} instances found in the given {@link BlockStatement}.
     */
    private static List<BooleanExpression> getBooleanExpressions(BlockStatement closureBlockStatement) {
        final List<Statement> statementList = closureBlockStatement.getStatements();

        List<BooleanExpression> booleanExpressions = new ArrayList<BooleanExpression>();

        for (Statement stmt : statementList)  {
            BooleanExpression tmp = null;

            if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() instanceof BooleanExpression)  {
                tmp = (BooleanExpression) ((ExpressionStatement) stmt).getExpression();
                tmp.setNodeMetaData("statementLabel", stmt.getStatementLabel());
            } else if (stmt instanceof ExpressionStatement)  {
                Expression expression = ((ExpressionStatement) stmt).getExpression();
                tmp = new BooleanExpression(expression);
                tmp.setSourcePosition(expression);
                tmp.setNodeMetaData("statementLabel", stmt.getStatementLabel());
            }

            booleanExpressions.add(tmp);
        }

        return booleanExpressions;
    }

    /**
     * Returns all {@link BooleanExpression} instances found in the given {@link BlockStatement}.
     */
    public static List<BooleanExpression> getBooleanExpressionsFromAssertionStatements(BlockStatement blockStatement) {
        AssertStatementCollector collector = new AssertStatementCollector();
        collector.visitBlockStatement(blockStatement);

        List<AssertStatement> assertStatements = collector.assertStatements;
        if (assertStatements.isEmpty()) return Collections.emptyList();

        List<BooleanExpression> booleanExpressions = new ArrayList<BooleanExpression>();
        for (AssertStatement assertStatement : assertStatements)  {
            booleanExpressions.add(assertStatement.getBooleanExpression());
        }

        return booleanExpressions;
    }

    public static BooleanExpression getBooleanExpression(List<BooleanExpression> booleanExpressions)  {
        if (booleanExpressions == null || booleanExpressions.isEmpty()) return new BooleanExpression(ConstantExpression.TRUE);

        BooleanExpression result = null;
        for (BooleanExpression booleanExpression : booleanExpressions)  {
            if (result == null) {
                result = booleanExpression;
            } else {
                result = new BooleanExpression(new BinaryExpression(result, Token.newSymbol(Types.LOGICAL_AND, -1, -1), booleanExpression));
            }
        }

        return result;
    }

    static class AssertStatementCollector extends ClassCodeVisitorSupport implements Opcodes {

        public List<AssertStatement> assertStatements = new ArrayList<AssertStatement>();

        @Override
        public void visitAssertStatement(AssertStatement statement) {
            assertStatements.add(statement);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }
}
