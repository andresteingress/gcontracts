package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.Message;
import org.gcontracts.annotations.meta.ContractElement;
import org.gcontracts.common.spi.AnnotationProcessingASTTransformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.Validate;

import java.util.List;

/**
 * Visits annotations of meta-type {@link ContractElement} and applies the AST transformations of the underlying
 * {@link AnnotationProcessingASTTransformation} implementation.
 *
 * @see AnnotationProcessingASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
public class AnnotationProcessingASTTransformationsVisitor extends BaseVisitor {

    public AnnotationProcessingASTTransformationsVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    protected AnnotationProcessingASTTransformationsVisitor() {}

    @Override
    public void visitClass(ClassNode type) {
        if (!CandidateChecks.isContractsCandidate(type)) return;
        super.visitClass(type);

        visitAnnotatedNode(type, null, null);
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        super.visitConstructorOrMethod(node, isConstructor);

        // not using visitAnnoatedNodes of super class since parameters would need to be
        // handled differently and context to methodNode would be lost
        visitAnnotatedNode(node, null, null);

        for (Parameter parameter : node.getParameters())  {
            visitAnnotatedNode(parameter, node.getDeclaringClass(), node);
        }
    }

    private void visitAnnotatedNode(AnnotatedNode annotatedNode, ClassNode classNode, MethodNode methodNode) {
        Validate.notNull(annotatedNode);

        final List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(annotatedNode, ContractElement.class.getName());

        for (AnnotationNode annotationNode : annotationNodes)  {
            final org.gcontracts.annotations.meta.AnnotationProcessingASTTransformation annotationProcessingAnno = (org.gcontracts.annotations.meta.AnnotationProcessingASTTransformation) annotationNode.getClassNode().getTypeClass().getAnnotation(org.gcontracts.annotations.meta.AnnotationProcessingASTTransformation.class);
            Class<? extends AnnotationProcessingASTTransformation> clz = annotationProcessingAnno.value();

            try {
                final AnnotationProcessingASTTransformation processor = clz.newInstance();
                if (annotatedNode instanceof ClassNode)  {
                    processor.process((ClassNode) annotatedNode);
                } else if (annotatedNode instanceof MethodNode)  {
                    MethodNode annotatedMethodNode = (MethodNode) annotatedNode;
                    processor.process(classNode, annotatedMethodNode);
                } else if (annotatedNode instanceof Parameter)  {
                    processor.process(classNode, methodNode, (Parameter) annotatedNode);
                }

            } catch (InstantiationException e) {
                getSourceUnit().getErrorCollector().addError(Message.create("Could not instantiate " + clz, getSourceUnit()), false);
            } catch (IllegalAccessException e) {
                getSourceUnit().getErrorCollector().addError(Message.create("Could not access " + clz, getSourceUnit()), false);
            }
        }
    }
}
