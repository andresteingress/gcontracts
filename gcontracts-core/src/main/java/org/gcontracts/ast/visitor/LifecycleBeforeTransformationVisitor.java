package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.common.spi.Lifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.util.LifecycleImplementationLoader;
import org.gcontracts.util.Validate;

/**
 * @author andre.steingress@gmail.com
 */
public class LifecycleBeforeTransformationVisitor extends BaseVisitor {

    private ProcessingContextInformation pci;

    public LifecycleBeforeTransformationVisitor(SourceUnit sourceUnit, ReaderSource source, final ProcessingContextInformation pci) {
        super(sourceUnit, source);

        Validate.notNull(pci);
        this.pci = pci;
    }

    protected LifecycleBeforeTransformationVisitor() {}

    @Override
    public void visitClass(ClassNode node) {
        super.visitClass(node);

        for (Lifecycle lifecyle : LifecycleImplementationLoader.load(Lifecycle.class, getClass().getClassLoader()))  {
            lifecyle.beforeProcessingClassNode(pci, node);
        }
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        for (Lifecycle lifecyle : LifecycleImplementationLoader.load(Lifecycle.class))  {
            lifecyle.beforeProcessingMethodNode(pci, node.getDeclaringClass(), node);
        }
    }
}