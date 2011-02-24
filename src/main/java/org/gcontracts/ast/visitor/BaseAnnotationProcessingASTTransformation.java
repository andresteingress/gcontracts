package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.gcontracts.annotations.meta.ClassInvariant;
import org.gcontracts.annotations.meta.Postcondition;
import org.gcontracts.annotations.meta.Precondition;
import org.gcontracts.common.spi.AnnotationProcessingASTTransformation;
import org.gcontracts.util.AnnotationUtils;
import org.gcontracts.util.Validate;

/**
 * Implements {@link AnnotationProcessingASTTransformation} and provides hooks for concrete
 * {@link org.codehaus.groovy.ast.AnnotatedNode} implementations.
 *
 * @see AnnotationProcessingASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
public abstract class BaseAnnotationProcessingASTTransformation implements AnnotationProcessingASTTransformation {

    public void process(ClassNode classNode, MethodNode methodNode, Parameter parameter) {}

    public void process(ClassNode classNode, MethodNode methodNode) {}

    public void process(ClassNode classNode) {}

    protected boolean hasPreconditionAnnotation(AnnotatedNode annotatedNode)  {
        Validate.notNull(annotatedNode);
        return AnnotationUtils.hasMetaAnnotations(annotatedNode, Precondition.class.getName()).size() > 0;
    }

    protected boolean hasPostconditionAnnotation(AnnotatedNode annotatedNode)  {
        Validate.notNull(annotatedNode);
        return AnnotationUtils.hasMetaAnnotations(annotatedNode, Postcondition.class.getName()).size() > 0;
    }

    protected boolean hasClassInvariantAnnotation(AnnotatedNode annotatedNode)  {
        Validate.notNull(annotatedNode);
        return AnnotationUtils.hasMetaAnnotations(annotatedNode, ClassInvariant.class.getName()).size() > 0;
    }
}
