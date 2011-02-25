package org.gcontracts.common.impl.lc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.common.base.BaseLifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.ClassInvariantGenerator;

/**
 * @author andre.steingress@gmail.com
 */
public class ClassInvariantLifecycle extends BaseLifecycle {

    @Override
    public void afterProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
        if (!processingContextInformation.isClassInvariantsEnabled()) return;
        if (!CandidateChecks.isContractsCandidate(classNode)) return;

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(processingContextInformation.readerSource());
        if (processingContextInformation.classInvariantClassNodes().isEmpty())  {
            classInvariantGenerator.generateDefaultInvariantAssertionMethod(classNode);
        }
    }

    @Override
    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
         if (!CandidateChecks.isPreOrPostconditionCandidate(classNode, methodNode)) return;
         if (processingContextInformation.classInvariantClassNodes().isEmpty()) return;

         final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(processingContextInformation.readerSource());
         classInvariantGenerator.addInvariantAssertionStatement(classNode, methodNode);
    }

    @Override
    public void afterProcessingContructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {
         if (!CandidateChecks.isPreOrPostconditionCandidate(classNode, constructorNode)) return;
         if (!processingContextInformation.isConstructorAssertionsEnabled()) return;
         if (processingContextInformation.classInvariantClassNodes().isEmpty()) return;

         final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(processingContextInformation.readerSource());
         classInvariantGenerator.addInvariantAssertionStatement(classNode, constructorNode);
    }
}
