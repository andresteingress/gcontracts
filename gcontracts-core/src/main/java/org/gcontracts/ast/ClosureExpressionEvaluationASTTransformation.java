package org.gcontracts.ast;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gcontracts.annotations.Contracted;
import org.gcontracts.ast.visitor.AnnotationClosureVisitor;
import org.gcontracts.ast.visitor.ConfigurationSetup;
import org.gcontracts.ast.visitor.ContractElementVisitor;
import org.gcontracts.generation.CandidateChecks;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates {@link org.codehaus.groovy.ast.expr.ClosureExpression} instances in as actual annotation parameters and
 * generates special contract closure classes from them.
 *
 * User: asteingress
 * Date: 10/12/12
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ClosureExpressionEvaluationASTTransformation extends BaseASTTransformation {

    private void generateAnnotationClosureClasses(SourceUnit unit, ReaderSource source, List<ClassNode> classNodes) {
        final AnnotationClosureVisitor annotationClosureVisitor = new AnnotationClosureVisitor(unit, source);

        for (final ClassNode classNode : classNodes)  {
            annotationClosureVisitor.visitClass(classNode);

            if (!CandidateChecks.isContractsCandidate(classNode)) continue;

            final ContractElementVisitor contractElementVisitor = new ContractElementVisitor(unit, source);
            contractElementVisitor.visitClass(classNode);

            if (!contractElementVisitor.isFoundContractElement()) continue;

            annotationClosureVisitor.visitClass(classNode);
            markClassNodeAsContracted(classNode);

            new ConfigurationSetup().init(classNode);
        }
    }

    /**
     * {@link org.codehaus.groovy.transform.ASTTransformation#visit(org.codehaus.groovy.ast.ASTNode[], org.codehaus.groovy.control.SourceUnit)}
     */
    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = unit.getAST();

        ReaderSource source = getReaderSource(unit);
        final List<ClassNode> classNodes = new ArrayList<ClassNode>(moduleNode.getClasses());

        generateAnnotationClosureClasses(unit, source, classNodes);
    }

    private void markClassNodeAsContracted(final ClassNode classNode) {
        final ClassNode contractedAnnotationClassNode = ClassHelper.makeWithoutCaching(Contracted.class);

        if (classNode.getAnnotations(contractedAnnotationClassNode).isEmpty())
            classNode.addAnnotation(new AnnotationNode(contractedAnnotationClassNode));
    }
}
