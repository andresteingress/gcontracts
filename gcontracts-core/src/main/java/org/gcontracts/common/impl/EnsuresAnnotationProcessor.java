package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.PostconditionGenerator;
import org.gcontracts.util.Validate;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class EnsuresAnnotationProcessor extends BaseAnnotationProcessingASTTransformation {

    protected static final String CLOSURE_ATTRIBUTE_NAME = "value";

    @Override
    public void process(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {
        Validate.notNull(processingContextInformation);
        Validate.notNull(classNode);
        Validate.notNull(methodNode);

        if (!processingContextInformation.isPostconditionsEnabled()) return;

        addPostcondition(processingContextInformation, classNode, methodNode);
    }

    public void addPostcondition(final ProcessingContextInformation processingContextInformation, final ClassNode type, final MethodNode method) {
        if (!CandidateChecks.isPreOrPostconditionCandidate(type, method)) return;

        final ReaderSource source = processingContextInformation.readerSource();
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        List<AnnotationNode> annotations = method.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                postconditionGenerator.generatePostconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                processingContextInformation.postconditionMethodNodes().add(method);
            }
        }
    }
}
