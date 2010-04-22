package org.gcontracts.ast;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class SetterInvariantValidationASTTransformation implements ASTTransformation {

    public void visit(ASTNode[] nodes, SourceUnit source) {
        final ModuleNode moduleNode = (ModuleNode)nodes[0];
        final List<ClassNode> classNodes = moduleNode.getClasses();

        for (final ClassNode classNode : classNodes)  {
            new DynamicSetterAssertionInjector(classNode).rewrite();
        }
    }
}
