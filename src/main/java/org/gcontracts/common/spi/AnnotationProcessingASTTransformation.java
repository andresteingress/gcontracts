package org.gcontracts.common.spi;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;

/**
 * @author andre.steingress@gmail.com
 */
public interface AnnotationProcessingASTTransformation {

    public void process(final ClassNode classNode, final MethodNode methodNode, final Parameter parameter);
    public void process(final ClassNode classNode, final MethodNode methodNode);
    public void process(final ClassNode classNode);

}
