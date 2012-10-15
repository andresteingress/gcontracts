package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Contracted;
import org.gcontracts.annotations.meta.ContractElement;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.util.AnnotationUtils;

/**
 * Checks whether the given {@link org.codehaus.groovy.ast.ClassNode} is relevant for
 * further processing.
 *
 * @author andre.steingress@gmail.com
 */
public class ContractElementVisitor extends BaseVisitor implements ASTNodeMetaData {

    private ClassNode classNode;
    private boolean foundContractElement = false;

    public ContractElementVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (!CandidateChecks.isContractsCandidate(node) && !CandidateChecks.isInterfaceContractsCandidate(node)) return;

        classNode = node;

        // check for the @Contracted shortcut
        if (AnnotationUtils.hasAnnotationOfType(node, Contracted.class.getName()))  {
            foundContractElement = true;
            return;
        }

        foundContractElement |= classNode.getNodeMetaData(CLOSURE_REPLACED) != null;

        if (!foundContractElement)  {
            super.visitClass(node);
        }

        // check base classes
        if (!foundContractElement && node.getSuperClass() != null)  {
            visitClass(node.getSuperClass());
        }

        // check interfaces
        if (!foundContractElement)  {
            for (ClassNode interfaceNode : node.getInterfaces())  {
                visitClass(interfaceNode);
                if (foundContractElement) return;
            }
        }
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode methodNode, boolean isConstructor) {
        if (!CandidateChecks.couldBeContractElementMethodNode(classNode, methodNode) && !(CandidateChecks.isPreconditionCandidate(classNode, methodNode))) return;
        foundContractElement |= methodNode.getNodeMetaData(CLOSURE_REPLACED) != null;
    }

    public boolean isFoundContractElement() {
        return foundContractElement;
    }
}
