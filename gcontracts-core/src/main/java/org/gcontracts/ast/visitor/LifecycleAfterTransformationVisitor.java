package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.common.spi.Lifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.util.LifecycleImplementationLoader;
import org.gcontracts.util.Validate;

import java.util.ArrayList;

/**
 * @author andre.steingress@gmail.com
 */
public class LifecycleAfterTransformationVisitor extends BaseVisitor {

    private ProcessingContextInformation pci;

    public LifecycleAfterTransformationVisitor(SourceUnit sourceUnit, ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);

        Validate.notNull(pci);
        this.pci = pci;
    }

    protected LifecycleAfterTransformationVisitor() {}

    @Override
    public void visitClass(ClassNode node) {
        super.visitClass(node);

        ArrayList<MethodNode> methods = new ArrayList<MethodNode>(node.getAllDeclaredMethods());
        ArrayList<MethodNode> constructors = new ArrayList<MethodNode>(node.getDeclaredConstructors());

        for (Lifecycle lifecyle : LifecycleImplementationLoader.load(Lifecycle.class, getClass().getClassLoader()))  {
            lifecyle.afterProcessingClassNode(pci, node);

            for (MethodNode constructor : constructors)  {
                lifecyle.afterProcessingContructorNode(pci, node, constructor);
            }

            for (MethodNode method: methods)  {
                lifecyle.afterProcessingMethodNode(pci, node, method);
            }
        }
    }
}
