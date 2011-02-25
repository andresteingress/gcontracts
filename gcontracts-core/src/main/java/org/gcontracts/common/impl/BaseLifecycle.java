package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.common.spi.Lifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;

/**
 * @author andre.steingress@gmail.com
 */
public abstract class BaseLifecycle implements Lifecycle {

    public void beforeProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {}

    public void afterProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {}

    public void beforeProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {}

    public void afterProcessingMethodNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode methodNode) {}

    public void beforeProcessingContructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {}

    public void afterProcessingContructorNode(ProcessingContextInformation processingContextInformation, ClassNode classNode, MethodNode constructorNode) {}
}
