package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.PostconditionGenerator;

/**
 * @author andre.steingress@gmail.com
 */
public class PostconditionLifecycle extends BaseLifecycle {

    @Override
    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
        if (!processingContextInformation.isPostconditionsEnabled()) return;
        if (!CandidateChecks.isPreOrPostconditionCandidate(classNode, methodNode)) return;

        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(processingContextInformation.readerSource());

        if (processingContextInformation.postconditionMethodNodes().contains(methodNode))  {
            postconditionGenerator.addOldVariablesMethod(classNode);
        } else {
            postconditionGenerator.generateDefaultPostconditionStatement(classNode, methodNode);
        }
    }
}
