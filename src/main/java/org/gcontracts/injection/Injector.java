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
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * Implemented by components that rewrite {@link org.codehaus.groovy.ast.ClassNode} instances.
 *
 * @see org.codehaus.groovy.ast.ClassNode
 *
 * @author andre.steingress@gmail.com
 */
public abstract class Injector {

    private static final String INVARIANT_CLOSURE_PREFIX = "$invariant$";

    /**
     * Rewrites the current {@link org.codehaus.groovy.ast.ClassNode}.
     */
    public abstract void rewrite();

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the field name of the invariant closure field of the given <tt>classNode</tt>
     */
    public String getInvariantClosureFieldName(final ClassNode classNode)  {
        return INVARIANT_CLOSURE_PREFIX + classNode.getNameWithoutPackage();
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the {@link org.codehaus.groovy.ast.FieldNode} which contains the invariant closure of the given <tt>classNode</tt>
     */
    public FieldNode getInvariantClosureFieldNode(final ClassNode classNode)  {
        return classNode.getField(getInvariantClosureFieldName(classNode));
    }

    /**
     * Decides whether the given <tt>constructorNode</tt> is a candidate for class invariant injection.
     *
     * @param constructorNode the {@link org.codehaus.groovy.ast.ConstructorNode} to check
     * @return whether the <tt>constructorNode</tt> is a candidate for injecting the class invariant or not
     */
    protected boolean isClassInvariantCandidate(final ConstructorNode constructorNode)  {
        return constructorNode != null &&
                constructorNode.isPublic() && !constructorNode.isStatic() && !constructorNode.isStaticConstructor();
    }

    /**
     * Decides whether the given <tt>methodNode</tt> is a candidate for class invariant injection.
     *
     * @param methodNode the {@link org.codehaus.groovy.ast.MethodNode} to check
     * @return whether the <tt>methodNode</tt> is a candidate for injecting the class invariant or not
     */
    protected boolean isClassInvariantCandidate(final MethodNode methodNode)  {
        return methodNode != null &&
                methodNode.isPublic() && !methodNode.isStatic() && !methodNode.isStaticConstructor() && !methodNode.isAbstract();
    }

    /**
     * Decides whether the given <tt>propertyNode</tt> is a candidate for class invariant injection.
     *
     * @param propertyNode the {@link org.codehaus.groovy.ast.PropertyNode} to check
     * @return whether the <tt>propertyNode</tt> is a candidate for injecting the class invariant or not
     */
    protected boolean isClassInvariantCandidate(final PropertyNode propertyNode)  {
        return propertyNode != null &&
                propertyNode.isPublic() && !propertyNode.isStatic() && !propertyNode.isInStaticContext() && !propertyNode.isClosureSharedVariable();
    }

    /**
     * Converts a {@link org.codehaus.groovy.ast.expr.ClosureExpression} into a String source.
     *
     * @param closureExpression the {@link org.codehaus.groovy.ast.expr.ClosureExpression} for retrieving the source-code from
     * @param source the {@link org.codehaus.groovy.control.io.ReaderSource} if the current source unit
     * @return the source the closure was created from
     */
    protected String convertClosureExpressionToSourceCode(ClosureExpression closureExpression, ReaderSource source) {

        if (source == null) {
            return "";
        }

        final int lineNumberStart = closureExpression.getLineNumber();
        final int lineNumberEnd   = closureExpression.getLastLineNumber();

        final StringBuilder builder = new StringBuilder();

        for (int i = lineNumberStart; i <= lineNumberEnd; i++)  {
            String line = source.getLine(i, null);
            if (line == null) return "";

            if (i == lineNumberStart && i != lineNumberEnd)  {
                builder.append(line.substring(closureExpression.getColumnNumber() - 1));
            } else if (i == lineNumberStart && i == lineNumberEnd)  {
                builder.append(line.substring(closureExpression.getColumnNumber() - 1, closureExpression.getLastColumnNumber() - 1));
            } else if (i == lineNumberEnd)  {
                builder.append(line.substring(0, closureExpression.getLastColumnNumber() - 1));
            } else {
                builder.append(line);
            }

            builder.append('\n');
        }

        String closureSource = builder.toString().trim();
        if (!closureSource.startsWith("{")) return "";

        return closureSource;
     }
}