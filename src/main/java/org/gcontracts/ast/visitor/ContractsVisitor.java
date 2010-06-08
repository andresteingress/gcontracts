package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;
import org.gcontracts.generation.PostconditionGenerator;
import org.gcontracts.generation.PreconditionGenerator;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.ClassInvariantGenerator;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class ContractsVisitor extends BaseVisitor {

    private ClosureExpression classInvariant;

    public ContractsVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode type) {

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);

        boolean found = false;

        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Invariant.class.getName()))  {
                classInvariant = (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME);

                classInvariantGenerator.generateInvariantAssertionStatement(type, classInvariant);

                // fix compilation with setting value() to java.lang.Object.class
                annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));

                found = true;
            }
        }

        if (!found)  {
            classInvariant = classInvariantGenerator.generateDefaultInvariantAssertionStatement(type);
        }

        super.visitClass(type);
    }

    @Override
    public void visitMethod(MethodNode method) {

        super.visitMethod(method);

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);
        final PreconditionGenerator preconditionGenerator = new PreconditionGenerator(source);
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        List<AnnotationNode> annotations = method.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Requires.class.getName()))  {
                preconditionGenerator.generatePreconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));

                // fix compilation with setting value() to java.lang.Object.class
                annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));
            } else if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                postconditionGenerator.generatePostconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));

                // fix compilation with setting value() to java.lang.Object.class
                annotation.setMember(CLOSURE_ATTRIBUTE_NAME, new ClassExpression(ClassHelper.OBJECT_TYPE));
            }
        }

        // If there is a class invariant we will append the check to this invariant
        // after each method call
        if (CandidateChecks.isClassInvariantCandidate(method))  {
            classInvariantGenerator.generateInvariantAssertionStatement(method, classInvariant);
        }
    }


}
