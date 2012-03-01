package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.util.AnnotationUtils;

import java.util.List;

/**
 * This {@link BaseVisitor} walks up the class hierarchy for the given {@link org.codehaus.groovy.ast.ClassNode}
 * and adds {@link org.gcontracts.annotations.meta.AnnotationContract} annotations to method parameters.
 * 
 * User: asteingress
 * Date: 2/29/12
 */
public class AnnotationContractParameterVisitor extends BaseVisitor {

    private ClassNode classNode;
    private List<MethodNode> methodNodes;
    
    private MethodNode currentMethodNode;
    
    public AnnotationContractParameterVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode node) {
        if (node == null) return;
        
        classNode = node;
        methodNodes = classNode.getMethods();
        
        // walk up the class hierarchy
        super.visitClass(node.getSuperClass());
        
        // walk through all interfaces
        for (ClassNode i : node.getAllInterfaces())  {
            
        }
    }

    @Override
    public void visitMethod(MethodNode node) {
        currentMethodNode = node;
        super.visitMethod(node);
        currentMethodNode = null;
    }

    @Override
    public void visitAnnotations(AnnotatedNode node) {
        if (!(node instanceof Parameter) || currentMethodNode == null) return;

        List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(node, "org.gcontracts.annotations.meta.AnnotationContract");
        
    }
}
