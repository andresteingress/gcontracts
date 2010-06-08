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
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.visitors.ClassInvariantVisitor;
import org.gcontracts.visitors.PostconditionVisitor;
import org.gcontracts.visitors.PreconditionVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Injects standard Java assertion statements which check the specified {@link org.gcontracts.annotations.Requires},
 * {@link org.gcontracts.annotations.Ensures} and {@link org.gcontracts.annotations.Invariant} closure annotations.
 *
 * @see org.gcontracts.annotations.Requires
 * @see org.gcontracts.annotations.Ensures
 * @see org.gcontracts.annotations.Invariant
 */
public class BasicAssertionInjector extends Injector {

    private static final String CLOSURE_ATTRIBUTE_NAME = "value";

    private final SourceUnit sourceUnit;
    private final ReaderSource source;
    private final ClassNode classNode;
    
    private ClosureExpression classInvariant;

    public BasicAssertionInjector(final SourceUnit sourceUnit, final ReaderSource source, final ClassNode classNode) {
        this.sourceUnit = sourceUnit;
        this.source = source;
        this.classNode = classNode;
    }

    /**
     * Rewrites the current {@link org.codehaus.groovy.ast.ClassNode} and adds assertions for all supported
     * assertion types.
     */
    public void rewrite() {
        new ClassCodeVisitorSupport() {

            @Override
            public void visitClass(ClassNode type) {

                final ClassInvariantVisitor classInvariantVisitor = new ClassInvariantVisitor(source);

                boolean found = false;

                List<AnnotationNode> annotations = type.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getName().equals(Invariant.class.getName()))  {
                        classInvariant = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);

                        classInvariantVisitor.generateInvariantAssertionStatement(type, classInvariant);

                        // fix compilation with setting value() to java.lang.Object.class
                        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

                        found = true;
                    }
                }

                if (!found)  {
                    classInvariant = classInvariantVisitor.generateDefaultInvariantAssertionStatement(type);
                }

                super.visitClass(type);
            }

            @Override
            public void visitMethod(MethodNode method) {

                super.visitMethod(method);

                final ClassInvariantVisitor classInvariantVisitor = new ClassInvariantVisitor(source);
                final PreconditionVisitor preconditionVisitor = new PreconditionVisitor(source);
                final PostconditionVisitor postconditionVisitor = new PostconditionVisitor(source);

                List<AnnotationNode> annotations = method.getAnnotations();
                for (AnnotationNode annotation: annotations)  {
                    if (annotation.getClassNode().getName().equals(Requires.class.getName()))  {
                        preconditionVisitor.generatePreconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));

                        // fix compilation with setting value() to java.lang.Object.class
                        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));
                    } else if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                        postconditionVisitor.generatePostconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));

                        // fix compilation with setting value() to java.lang.Object.class
                        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));
                    }
                }

                // If there is a class invariant we will append the check to this invariant
                // after each method call
                if (CandidateChecks.isClassInvariantCandidate(method))  {
                    classInvariantVisitor.generateInvariantAssertionStatement(method, classInvariant);
                }
            }

            protected SourceUnit getSourceUnit() {
                return null;
            }
        }.visitClass(classNode);
    }
}
