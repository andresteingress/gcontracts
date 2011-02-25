package org.gcontracts.common.spi;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * @author andre.steingress@gmail.com
 */
public interface Lifecycle {

    public void beforeProcessingClassNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode);
    public void afterProcessingClassNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode);

    public void beforeProcessingMethodNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode methodNode);
    public void afterProcessingMethodNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode methodNode);

    public void beforeProcessingContructorNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode constructorNode);
    public void afterProcessingContructorNode(final ProcessingContextInformation processingContextInformation, final ClassNode classNode, final MethodNode constructorNode);
}
