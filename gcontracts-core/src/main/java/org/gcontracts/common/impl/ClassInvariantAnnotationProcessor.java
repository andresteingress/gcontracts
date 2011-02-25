package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.ClassInvariantGenerator;
import org.gcontracts.util.Validate;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class ClassInvariantAnnotationProcessor extends BaseAnnotationProcessingASTTransformation {

    protected static final String CLOSURE_ATTRIBUTE_NAME = "value";

    @Override
    public void process(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
        Validate.notNull(processingContextInformation);
        Validate.notNull(classNode);

        if (!processingContextInformation.isClassInvariantsEnabled()) return;

        addClassInvariant(processingContextInformation, classNode);
    }

    public void addClassInvariant(final ProcessingContextInformation processingContextInformation, final ClassNode type) {
        final ReaderSource source = processingContextInformation.readerSource();
        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);

        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Invariant.class.getName()))  {
                classInvariantGenerator.generateInvariantAssertionStatement(type, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME), false);
                processingContextInformation.classInvariantClassNodes().add(type);
            }
        }
    }
}
