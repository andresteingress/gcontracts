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
package org.gcontracts.ast;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gcontracts.annotations.EnableAssertions;
import org.gcontracts.ast.visitor.*;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Custom AST transformation that removes closure annotations of {@link org.gcontracts.annotations.Invariant},
 * {@link org.gcontracts.annotations.Requires} and {@link org.gcontracts.annotations.Ensures} and adds Java
 * assertions executing the closure-code.
 * </p>
 * <p>
 * Whenever an assertion is broken an {@link org.gcontracts.AssertionViolation} descendant class will be thrown.
 * </p>
 *
 * @see org.gcontracts.PreconditionViolation
 * @see org.gcontracts.PostconditionViolation
 * @see org.gcontracts.ClassInvariantViolation
 *
 * @author ast
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class GContractsASTTransformation extends BaseASTTransformation {

    private void generateAnnotationClosureClasses(SourceUnit unit, ReaderSource source, List<ClassNode> classNodes) {
        final AnnotationClosureVisitor annotationClosureVisitor = new AnnotationClosureVisitor(unit, source);

        for (final ClassNode classNode : classNodes)  {
            annotationClosureVisitor.visitClass(classNode);
        }
    }

    /**
     * {@link org.codehaus.groovy.transform.ASTTransformation#visit(org.codehaus.groovy.ast.ASTNode[], org.codehaus.groovy.control.SourceUnit)}
     */
    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = unit.getAST();

        ReaderSource source = getReaderSource(unit);
        final List<ClassNode> classNodes = new ArrayList<ClassNode>(moduleNode.getClasses());

        generateAnnotationClosureClasses(unit, source, classNodes);

        // at this point all classes in this module should not contain closure expressions as annotation arguments
        final ConfiguratorSetupVisitor configuratorSetupVisitor = new ConfiguratorSetupVisitor(unit, source);

        for (final ClassNode classNode : moduleNode.getClasses())  {
            // there is nothing to do for interfaces
            if (!CandidateChecks.isContractsCandidate(classNode)) continue;

            final ProcessingContextInformation pci = createProcessingContextInformation(classNode, unit, source);
            if (pci == null) continue;

            configuratorSetupVisitor.visitClass(classNode);

            new LifecycleBeforeTransformationVisitor(unit, source, pci).visitClass(classNode);

            new AnnotationProcessorVisitor(unit, source, pci).visitClass(classNode);
            new DomainModelInjectionVisitor(unit, source, pci).visitClass(classNode);

            new LifecycleAfterTransformationVisitor(unit, source, pci).visitClass(classNode);
            new DynamicSetterInjectionVisitor(unit, source).visitClass(classNode);
        }
    }

    private ProcessingContextInformation createProcessingContextInformation(ClassNode classNode, SourceUnit unit, ReaderSource source)  {
        final ClassNode assertionsEnabledClassNode = ClassHelper.makeWithoutCaching(EnableAssertions.class);

        final List<AnnotationNode> annotationsPackage = classNode.getPackage() != null ? classNode.getPackage().getAnnotations(assertionsEnabledClassNode) : Collections.<AnnotationNode>emptyList();
        final List<AnnotationNode> annotationsClassNode = classNode.getAnnotations(assertionsEnabledClassNode);

        if (annotationsPackage.isEmpty() && annotationsClassNode.isEmpty())  return null;

        return new ProcessingContextInformation(classNode, unit, source);
    }
}

