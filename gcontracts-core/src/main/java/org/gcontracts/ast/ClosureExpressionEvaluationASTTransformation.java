package org.gcontracts.ast;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gcontracts.ast.visitor.AnnotationClosureVisitor;

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
}
