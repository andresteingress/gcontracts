package org.gcontracts.spring;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.gcontracts.common.base.BaseLifecycle;
import org.gcontracts.common.spi.ProcessingContextInformation;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.generation.ClassInvariantGenerator;
import org.gcontracts.util.AnnotationUtils;
import org.objectweb.asm.Opcodes;

/**
 * Implements a {@link org.gcontracts.common.spi.Lifecycle} in order to intercept GContracts
 * assertion generation mechanism and adapt it to specific requirements of the Spring
 * application container.<p/>
 *
 * @author andre.steingress@gmail.com
 */
public class SpringContractsLifecycle extends BaseLifecycle {

    private static final String INITIALIZINGBEAN_INTERFACE = "org.springframework.beans.factory.InitializingBean";
    private static final String POSTCONSTRUCT_ANNOTATION = "javax.annotation.PostConstruct";

    private static final String POSTCONSTRUCT_METHOD_NAME = "$gcontracts_postConstruct";
    private static final String SPRING_STEREOTYPE_PACKAGE = "org.springframework.stereotype";

    private static final String IS_SPRING_STEREOTYPE = "isSpringStereotype";

    @Override
    public void beforeProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
        if (!CandidateChecks.isContractsCandidate(classNode)) return;

        boolean isSpringStereotype = AnnotationUtils.hasAnnotationOfType(classNode, SPRING_STEREOTYPE_PACKAGE);

        processingContextInformation.put(IS_SPRING_STEREOTYPE, isSpringStereotype);
        processingContextInformation.setConstructorAssertionsEnabled(!isSpringStereotype);
    }

    @Override
    public void afterProcessingClassNode(ProcessingContextInformation processingContextInformation, ClassNode classNode) {
        if (!CandidateChecks.isContractsCandidate(classNode)) return;
        if (!(Boolean) processingContextInformation.get(IS_SPRING_STEREOTYPE)) return;
        if (processingContextInformation.classInvariantClassNodes().isEmpty()) return;

        createPostConstructMethodForSpringBeans(processingContextInformation, classNode);
    }

    private void createPostConstructMethodForSpringBeans(ProcessingContextInformation pci, ClassNode type) {
        if (isAnyPostConstructionCallbackAvailable(type)) return;

        // add a synthetic post-construction method
        final MethodNode postConstructionMethodNode =
                type.addMethod(POSTCONSTRUCT_METHOD_NAME, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());

        final Class<?> postConstructAnnotationClass;
        try {
            postConstructAnnotationClass = SpringContractsLifecycle.class.getClassLoader().loadClass(POSTCONSTRUCT_ANNOTATION);

            final ClassNode postConstructAnnotationClassNode = ClassHelper.makeWithoutCaching(postConstructAnnotationClass);
            postConstructionMethodNode.addAnnotation(new AnnotationNode(postConstructAnnotationClassNode));

            final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(pci.readerSource());
            classInvariantGenerator.addInvariantAssertionStatement(type, postConstructionMethodNode);

        } catch (ClassNotFoundException e) {
            pci.addError("Annotation " + POSTCONSTRUCT_ANNOTATION + " could not be found in classpath!", type);
        }
    }

    private boolean isAnyPostConstructionCallbackAvailable(ClassNode type)  {

        boolean foundAnyPostConstructionCallback = false;

        // 1: the bean implements InitializingBean
        for (ClassNode interfaceClassNode : type.getAllInterfaces())  {
            if (interfaceClassNode.getName().equals(INITIALIZINGBEAN_INTERFACE))  {
                foundAnyPostConstructionCallback = true;
                break;
            }
        }

        // 2: method annotated with @PostConstruct (JEE5)
        for (MethodNode methodNode : type.getAllDeclaredMethods())  {
            if (AnnotationUtils.hasAnnotationOfType(methodNode, POSTCONSTRUCT_ANNOTATION))  {
                foundAnyPostConstructionCallback = true;
                break;
            }
        }

        return foundAnyPostConstructionCallback;
    }
}
