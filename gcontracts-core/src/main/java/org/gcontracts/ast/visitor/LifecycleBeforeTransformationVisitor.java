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
package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.common.spi.Lifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.util.LifecycleImplementationLoader;
import org.gcontracts.util.Validate;

import java.util.ArrayList;

/**
 * @author andre.steingress@gmail.com
 */
public class LifecycleBeforeTransformationVisitor extends BaseVisitor {

    private ProcessingContextInformation pci;

    public LifecycleBeforeTransformationVisitor(SourceUnit sourceUnit, ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);

        Validate.notNull(pci);
        this.pci = pci;
    }

    protected LifecycleBeforeTransformationVisitor() {}

    @Override
    public void visitClass(ClassNode node) {
        super.visitClass(node);

        ArrayList<MethodNode> methods = new ArrayList<MethodNode>(node.getMethods());
        ArrayList<MethodNode> constructors = new ArrayList<MethodNode>(node.getDeclaredConstructors());

        for (Lifecycle lifecyle : LifecycleImplementationLoader.load(Lifecycle.class, getClass().getClassLoader()))  {
            lifecyle.beforeProcessingClassNode(pci, node);

            for (MethodNode constructor : constructors)  {
                lifecyle.beforeProcessingContructorNode(pci, node, constructor);
            }

            for (MethodNode method: methods)  {
                lifecyle.beforeProcessingMethodNode(pci, node, method);
            }
        }
    }
}
