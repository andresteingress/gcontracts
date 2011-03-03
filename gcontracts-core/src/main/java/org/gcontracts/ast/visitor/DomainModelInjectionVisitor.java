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
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.domain.ClassInvariant;
import org.gcontracts.domain.Contract;
import org.gcontracts.domain.Postcondition;
import org.gcontracts.domain.Precondition;
import org.gcontracts.generation.ClassInvariantGenerator;
import org.gcontracts.generation.PostconditionGenerator;
import org.gcontracts.generation.PreconditionGenerator;
import org.gcontracts.util.Validate;

import java.util.Map;

/**
 * Visits the given {@link ClassNode} and injects the current {@link org.gcontracts.domain.Contract} to the current AST
 * nodes.
 *
 * @see org.gcontracts.domain.Contract
 *
 * @author ast
 */
public class DomainModelInjectionVisitor extends BaseVisitor {

    private final ProcessingContextInformation pci;
    private final Contract contract;

    public DomainModelInjectionVisitor(final SourceUnit sourceUnit, final ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);
        Validate.notNull(pci);
        Validate.notNull(pci.contract());

        this.pci = pci;
        this.contract = pci.contract();
    }

    @Override
    public void visitClass(ClassNode type) {
        injectClassInvariant(type, contract.classInvariant());

        for (Map.Entry<MethodNode, Precondition> entry : contract.preconditions())  {
            injectPrecondition(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<MethodNode, Postcondition> entry : contract.postconditions())  {
            injectPostcondition(entry.getKey(), entry.getValue());
        }
    }

    public void injectClassInvariant(final ClassNode type, final ClassInvariant classInvariant) {
        if (!pci.isClassInvariantsEnabled()) return;

        final ReaderSource source = pci.readerSource();
        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);

        classInvariantGenerator.generateInvariantAssertionStatement(type, classInvariant.booleanExpression());
    }

    public void injectPrecondition(final MethodNode method, final Precondition precondition) {
        if (!pci.isPreconditionsEnabled() || method.isAbstract()) return;

        final ReaderSource source = pci.readerSource();
        final PreconditionGenerator preconditionGenerator = new PreconditionGenerator(source);

        preconditionGenerator.generatePreconditionAssertionStatement(method, precondition.booleanExpression());
    }

    public void injectPostcondition(final MethodNode method, final Postcondition postcondition) {
        if (!pci.isPostconditionsEnabled() || method.isAbstract()) return;

        final ReaderSource source = pci.readerSource();
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        postconditionGenerator.generatePostconditionAssertionStatement(method, postcondition.booleanExpression(), postcondition.isPartOfConstructor());
    }
}
