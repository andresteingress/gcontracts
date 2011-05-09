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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
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
     * @param booleanExpressions the assertion's {@link org.codehaus.groovy.ast.expr.BooleanExpression} instances
     *
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static BlockStatement getAssertionStatemens(final List<BooleanExpression> booleanExpressions)  {

        List<AssertStatement> assertStatements = new ArrayList<AssertStatement>();
        for (BooleanExpression booleanExpression : booleanExpressions)  {
            assertStatements.add(getAssertionStatement(booleanExpression));
        }

        final BlockStatement blockStatement = new BlockStatement();
        blockStatement.getStatements().addAll(assertStatements);

        return blockStatement;
    }

    /**
     * Reusable method for creating assert statements for the given <tt>booleanExpression</tt>.
     *
     * @param booleanExpression the assertion's {@link org.codehaus.groovy.ast.expr.BooleanExpression}
     *
     * @return a newly created {@link org.codehaus.groovy.ast.stmt.AssertStatement}
     */
    public static AssertStatement getAssertionStatement(final BooleanExpression booleanExpression)  {

        final AssertStatement assertStatement = new AssertStatement(booleanExpression);
        assertStatement.setStatementLabel((String) booleanExpression.getNodeMetaData("statementLabel"));
        assertStatement.setSourcePosition(booleanExpression);

        return assertStatement;
    }

    /**
     * Gets a list of {@link org.codehaus.groovy.ast.stmt.ReturnStatement} instances from the given {@link MethodNode}.
     *
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} that holds the given <tt>lastStatement</tt>
     * @return a {@link org.codehaus.groovy.ast.stmt.ReturnStatement} or <tt>null</tt>
     */
    public static List<ReturnStatement> getReturnStatements(MethodNode method)  {

        final ReturnStatementVisitor returnStatementVisitor = new ReturnStatementVisitor();
        returnStatementVisitor.visitMethod(method);

        final List<ReturnStatement> returnStatements = returnStatementVisitor.getReturnStatements();
        final BlockStatement blockStatement = (BlockStatement) method.getCode();

        if (returnStatements.isEmpty())  {
            final int statementCount = blockStatement.getStatements().size();
            if (statementCount > 0)  {
                final Statement lastStatement = blockStatement.getStatements().get(statementCount - 1);
                if (lastStatement instanceof ExpressionStatement)  {
                    final ReturnStatement returnStatement = new ReturnStatement((ExpressionStatement) lastStatement);
                    returnStatement.setSourcePosition(lastStatement);

                    blockStatement.getStatements().remove(lastStatement);
                    blockStatement.addStatement(returnStatement);

                    returnStatements.add(returnStatement);
                }
            }
        }

        return returnStatements;
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

    public static void injectResultVariableReturnStatementAndAssertionCallStatement(BlockStatement statement, ReturnStatement returnStatement, BlockStatement assertionCallStatement)  {
        final AddResultReturnStatementVisitor addResultReturnStatementVisitor = new AddResultReturnStatementVisitor(returnStatement, assertionCallStatement);
        addResultReturnStatementVisitor.visitBlockStatement(statement);
    }

    public static void addAssertionCallStatementToReturnStatement(BlockStatement statement, ReturnStatement returnStatement, Statement assertionCallStatement)  {
        final AddAssertionCallStatementToReturnStatementVisitor addAssertionCallStatementToReturnStatementVisitor = new AddAssertionCallStatementToReturnStatementVisitor(returnStatement, assertionCallStatement);
        addAssertionCallStatementToReturnStatementVisitor.visitBlockStatement(statement);
    }

    /**
     * Collects all {@link ReturnStatement} instances from a given code block.
     */
    public static class ReturnStatementVisitor extends ClassCodeVisitorSupport {

        private List<ReturnStatement> returnStatements = new ArrayList<ReturnStatement>();

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            returnStatements.add(statement);
        }

        public List<ReturnStatement> getReturnStatements() {
            return returnStatements;
        }
    }

    /**
     * Replaces a given {@link ReturnStatement} with the appropriate assertion call statement and returns a result variable expression.
     */
    public static class AddResultReturnStatementVisitor extends ClassCodeVisitorSupport {

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private BlockStatement blockStatement;
        private BlockStatement blockStatementCopy;

        private final ReturnStatement returnStatement;
        private final BlockStatement assertionCallStatement;

        public AddResultReturnStatementVisitor(ReturnStatement returnStatement, BlockStatement assertionCallStatement)  {
            this.returnStatement = returnStatement;
            this.assertionCallStatement = assertionCallStatement;
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {
            blockStatement = block;

            blockStatementCopy = new BlockStatement(new ArrayList<Statement>(blockStatement.getStatements()), blockStatement.getVariableScope());
            blockStatementCopy.copyNodeMetaData(blockStatement);
            blockStatementCopy.setSourcePosition(blockStatement);

            for (Statement statement : blockStatementCopy.getStatements())  {
                if (statement == returnStatement) {
                    blockStatement.getStatements().remove(statement);
                    blockStatement.addStatements(assertionCallStatement.getStatements());
                    blockStatement.addStatement(new ReturnStatement(new VariableExpression("result")));
                    return; // we found the return statement under target, let's cancel tree traversal
                }
            }

            super.visitBlockStatement(blockStatement);
        }
    }

    /**
     * Replaces a given {@link ReturnStatement} with the appropriate assertion call statement and returns a result variable expression.
     */
    public static class AddAssertionCallStatementToReturnStatementVisitor extends ClassCodeVisitorSupport {

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        private BlockStatement blockStatement;
        private BlockStatement blockStatementCopy;

        private final ReturnStatement returnStatement;
        private final Statement assertionCallStatement;

        public AddAssertionCallStatementToReturnStatementVisitor(ReturnStatement returnStatement, Statement assertionCallStatement)  {
            this.returnStatement = returnStatement;
            this.assertionCallStatement = assertionCallStatement;
        }

        @Override
        public void visitBlockStatement(BlockStatement block) {
            blockStatement = block;

            blockStatementCopy = new BlockStatement(new ArrayList<Statement>(blockStatement.getStatements()), blockStatement.getVariableScope());
            blockStatementCopy.copyNodeMetaData(blockStatement);
            blockStatementCopy.setSourcePosition(blockStatement);

            for (Statement statement : blockStatementCopy.getStatements())  {
                if (statement == returnStatement) {
                    blockStatement.getStatements().remove(statement);

                    final VariableExpression $_gc_result = new VariableExpression("$_gc_result", ClassHelper.DYNAMIC_TYPE);
                    blockStatement.addStatement(new ExpressionStatement(
                            new DeclarationExpression($_gc_result, Token.newSymbol(Types.ASSIGN, -1, -1), returnStatement.getExpression())
                    ));

                    blockStatement.addStatement(assertionCallStatement);

                    ReturnStatement gcResultReturn = new ReturnStatement($_gc_result);
                    gcResultReturn.setSourcePosition(returnStatement);

                    blockStatement.addStatement(gcResultReturn);
                    return; // we found the return statement under target, let's cancel tree traversal
                }
            }

            super.visitBlockStatement(blockStatement);
        }
    }
}
