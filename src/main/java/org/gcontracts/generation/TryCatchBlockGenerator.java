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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.powerassert.PowerAssertionError;

/**
 * Creates a try-catch block around a given {@link org.codehaus.groovy.ast.stmt.AssertStatement} and catches
 * a {@link org.codehaus.groovy.transform.powerassert.PowerAssertionError} to reuse the generated visual output.
 *
 * @author andre.steingress@gmail.com
 */
public class TryCatchBlockGenerator {

   public static Statement generateTryCatchStatement(final String message, final AssertStatement assertStatement)  {

       final TryCatchStatement tryCatchStatement = new TryCatchStatement(assertStatement, new EmptyStatement());
       final BlockStatement catchBlock = new BlockStatement();

       ExpressionStatement expr = new ExpressionStatement(new DeclarationExpression(new VariableExpression("newError", ClassHelper.makeWithoutCaching(PowerAssertionError.class)), Token.newSymbol(Types.ASSIGN, -1, -1),
               new ConstructorCallExpression(ClassHelper.makeWithoutCaching(PowerAssertionError.class),
                       new ArgumentListExpression(new BinaryExpression(new ConstantExpression(message), Token.newSymbol(Types.PLUS, -1, -1), new MethodCallExpression(new VariableExpression(new Parameter(ClassHelper.makeWithoutCaching(PowerAssertionError.class), "error")), "getMessage", ArgumentListExpression.EMPTY_ARGUMENTS))))));

       ExpressionStatement exp2 = new ExpressionStatement(new MethodCallExpression(new VariableExpression("newError", ClassHelper.makeWithoutCaching(PowerAssertionError.class)), "setStackTrace", new ArgumentListExpression(
               new MethodCallExpression(new VariableExpression(new Parameter(ClassHelper.makeWithoutCaching(PowerAssertionError.class), "error")), "getStackTrace", ArgumentListExpression.EMPTY_ARGUMENTS)
       )));


       ThrowStatement throwStatement = new ThrowStatement(new VariableExpression("newError", ClassHelper.makeWithoutCaching(PowerAssertionError.class)));

       catchBlock.addStatement(expr);
       catchBlock.addStatement(exp2);
       catchBlock.addStatement(throwStatement);

       tryCatchStatement.addCatch(new CatchStatement(new Parameter(ClassHelper.makeWithoutCaching(PowerAssertionError.class), "error"), catchBlock));

       return tryCatchStatement;
   }
}
