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
package org.gcontracts.common.spi;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * <p>Specifies life-cycle hook-ins for applying AST transformation logic before and
 * after the annotation processors have been run.</p>
 *
 * <p>During excution of GContracts AST transformations, the following process is applied on each {@link ClassNode}
 * instance which qualifies for contract annotations:</P>
 *
 * <ol>
 *    <li>Generation of closure classes.</li>
 *    <li>Handling of {@link AnnotationProcessor} implementation classes</li>
 *    <li>Domain Model Conversion and Injection</li>
 * </ol>
 *
 * <h3>Generation of closure classes</h3>
 *
 * <p>In order to support Groovy 1.7.x GContracts backported Groovy 1.8 handling of annotation closures. This is done
 * by extracting {@link org.codehaus.groovy.ast.expr.ClosureExpression} from annotations and creating {@link groovy.lang.Closure}
 * implementation classes.</p>
 *
 * <h3>Handling of AnnotationProcessor implementation classes</h3>
 *
 * <p>{@link AnnotationProcessor} implementatios are used to modify domain classes found in <tt>org.gcontracts.domain</tt>. For that
 * reason, concrete annotation processor often don't modify AST nodes directly, but simply work with domain classes like
 * {@link org.gcontracts.domain.Contract}. Whenever an annotation processor is done, it has finished its work on the
 * underlying domain model. </p>
 *
 * <p>{@link #beforeProcessingClassNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode)},
 * {@link #beforeProcessingMethodNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)},
 * {@link #beforeProcessingContructorNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)} are fired
 * before annotation processors are executed.</p>
 *
 * <h3>Domain Model Conversion and Injection</h3>
 *
 * <p>Takes a look at the domain model instances and generates the corresponding AST transformation code.</p>
 *
 * <p>{@link #afterProcessingClassNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode)},
 * {@link #afterProcessingMethodNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)},
 * {@link #afterProcessingContructorNode(ProcessingContextInformation, org.codehaus.groovy.ast.ClassNode, org.codehaus.groovy.ast.MethodNode)} are fired
 * after domain model conversion and injection is done.</p>
 *
 * @author ast
 */
public interface Lifecycle {

    public void beforeProcessingClassNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode);
    public void afterProcessingClassNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode);

    public void beforeProcessingMethodNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode methodNode);
    public void afterProcessingMethodNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode methodNode);

    public void beforeProcessingContructorNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode constructorNode);
    public void afterProcessingContructorNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode constructorNode);
}
