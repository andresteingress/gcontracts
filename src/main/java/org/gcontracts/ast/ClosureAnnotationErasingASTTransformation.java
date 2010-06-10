package org.gcontracts.ast;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.gcontracts.ast.visitor.ContractsErasingVisitor;

/**
 * <p>
 * Custom AST transformation that erases all closure annotation parameters.
 * </p>
 *
 * @see org.gcontracts.ast.BaseASTTransformation
 * @see org.codehaus.groovy.transform.ASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ClosureAnnotationErasingASTTransformation extends BaseASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit unit) {
        final ModuleNode moduleNode = (ModuleNode)nodes[0];

        ReaderSource source = getReaderSource(unit);

        for (final ClassNode classNode : moduleNode.getClasses())  {
            new ContractsErasingVisitor(unit, source).visitClass(classNode);
        }
    }
}