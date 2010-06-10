package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;

import java.util.List;

/**
 * <p>
 * Erases GContracts closure annotations from {@link org.gcontracts.annotations.Requires}, {@link org.gcontracts.annotations.Ensures}
 * and {@link org.gcontracts.annotations.Invariant}.
 * </p>
 *
 * @see org.gcontracts.annotations.Requires
 * @see org.gcontracts.annotations.Ensures
 * @see org.gcontracts.annotations.Invariant
 *
 * @see org.gcontracts.ast.visitor.BaseVisitor
 *
 * @author andre.steingress@gmail.com
 */
public class ContractsErasingVisitor extends BaseVisitor {

    public ContractsErasingVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode type) {

        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Invariant.class.getName()))  {
                eraseClosureParameter(annotation);
            }
        }

        super.visitClass(type);
    }

    @Override
    public void visitMethod(MethodNode method) {

        super.visitMethod(method);

        List<AnnotationNode> annotations = method.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Requires.class.getName()))  {
                eraseClosureParameter(annotation);
            } else if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                eraseClosureParameter(annotation);
            }
        }
    }

    protected void eraseClosureParameter(AnnotationNode annotation) {
        annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));
    }
}