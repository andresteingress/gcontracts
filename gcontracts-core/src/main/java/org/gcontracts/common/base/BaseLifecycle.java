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
package org.gcontracts.common.base;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.gcontracts.common.spi.Lifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.BaseGenerator;
import org.objectweb.asm.Opcodes;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Base implementation class for interface {@link Lifecycle}. This class is supposed
 * tp be extended by {@link Lifecycle} implementation classes and provides empty
 * method bodies for all interface methods.
 *
 * @see Lifecycle
 *
 * @author ast
 */
public abstract class BaseLifecycle implements Lifecycle {

    public void beforeProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
        addConcurrentLockField(classNode);
    }

    private void addConcurrentLockField(final ClassNode classNode) {
        final ClassNode reentrantLockClassNode = ClassHelper.make(ReentrantLock.class);

        if (classNode.getDeclaredField(BaseGenerator.LOCK_FIELD_NAME) == null)
            classNode.addField(BaseGenerator.LOCK_FIELD_NAME, Opcodes.ACC_PRIVATE, reentrantLockClassNode, new ConstructorCallExpression(reentrantLockClassNode, ArgumentListExpression.EMPTY_ARGUMENTS));
    }

    public void afterProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {}

    public void beforeProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {}

    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {}

    public void beforeProcessingContructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {}

    public void afterProcessingContructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {}
}
