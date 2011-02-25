package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.PreconditionGenerator;

/**
 * @author andre.steingress@gmail.com
 */
public class PreconditionLifecycle extends BaseLifecycle {

    @Override
    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
        if (!processingContextInformation.isPreconditionsEnabled()) return;
        if (!CandidateChecks.isPreOrPostconditionCandidate(classNode, methodNode)) return;
        if (processingContextInformation.preconditionMethodNodes().contains(methodNode)) return;

        final PreconditionGenerator preconditionGenerator = new PreconditionGenerator(processingContextInformation.readerSource());
        preconditionGenerator.generateDefaultPreconditionStatement(classNode, methodNode);
    }
}
