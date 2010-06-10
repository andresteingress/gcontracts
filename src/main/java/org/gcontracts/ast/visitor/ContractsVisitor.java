package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.ClassInvariantGenerator;
import org.gcontracts.generation.PostconditionGenerator;
import org.gcontracts.generation.PreconditionGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Main visitor in GContracts extending {@link org.gcontracts.ast.visitor.BaseVisitor}. At runtime visitors of
 * this class are used to generate and add class invariants, pre- and postconditions to annotated classes and methods.
 * </p>
 *
 * @see org.gcontracts.ast.visitor.BaseVisitor
 *
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

                found = true;
            }
        }

        if (!found)  {
            classInvariant = classInvariantGenerator.generateDefaultInvariantAssertionStatement(type);
        }

        super.visitClass(type);

        for (final MethodNode methodNode : type.getAllDeclaredMethods())  {
            addPreOrPostcondition(type, methodNode);
        }
    }

    public void addPreOrPostcondition(final ClassNode type, final MethodNode method) {

        if (!CandidateChecks.isPreOrPostconditionCandidate(type, method)) return;

        boolean preconditionFound = false;
        boolean postconditionFound = false;

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);
        final PreconditionGenerator preconditionGenerator = new PreconditionGenerator(source);
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        List<AnnotationNode> annotations = method.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Requires.class.getName()))  {
                preconditionGenerator.generatePreconditionAssertionStatement(type, method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                preconditionFound = true;
            } else if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                postconditionGenerator.generatePostconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                postconditionFound = true;
            }
        }

        // adding a default precondition if an inherited precondition is found
        if (!preconditionFound)  {
            preconditionGenerator.generateDefaultPreconditionStatement(type, method);
        }

        // adding a default postcondition if an inherited postcondition is found
        if (!postconditionFound)  {
            postconditionGenerator.generateDefaultPostconditionStatement(type, method);
        }

        // If there is a class invariant we will append the check to this invariant
        // after each method call
        if (CandidateChecks.isClassInvariantCandidate(method))  {
            classInvariantGenerator.generateInvariantAssertionStatement(type, method, classInvariant);
        }
    }


}
