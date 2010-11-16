/**
 * Copyright (c) 2010, gcontracts@me.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.annotations.Ensures;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.annotations.Requires;
import org.gcontracts.generation.*;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * <p>
 * Main visitor in GContracts extending {@link org.gcontracts.ast.visitor.BaseVisitor}. At runtime visitors of
 * this class are used to generate and add class invariants, pre- and postconditions to annotated classes and methods.
 * </p>
 *
 * @see org.gcontracts.ast.visitor.BaseVisitor
 *
 * @author andre.steingress@gmail.com
 */
public class ContractsVisitor extends BaseVisitor {

    private boolean hasClassInvariant = false;
    private boolean isSpringComponent = false;

    private static final String INITIALIZINGBEAN_INTERFACE = "org.springframework.beans.factory.InitializingBean";
    private static final String AFTER_PROPERTIES_SET = "afterPropertiesSet";
    private static final String SPRING_STEREOTYPE_PACKAGE = "org.springframework.stereotype";

    public ContractsVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    @Override
    public void visitClass(ClassNode type) {

        if (!CandidateChecks.isContractsCandidate(type)) return;

        addConfigurationVariable(type);
        addClassInvariant(type);

        super.visitClass(type);

        addPreOrPostconditionsToConstructors(type);
        addPreOrPostconditionsToDeclaredMethods(type);

        addPreOrPostconditionsToAfterPropertiesSet(type);
    }

    private void addClassInvariant(ClassNode type) {
        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);

        List<AnnotationNode> annotations = type.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            // if this is a spring component, we will have to handle constructor invariants differently
            if (annotation.getClassNode().getName().startsWith(SPRING_STEREOTYPE_PACKAGE))  {
                isSpringComponent = true;
            }

            if (annotation.getClassNode().getName().equals(Invariant.class.getName()))  {
                // Generates a synthetic method holding the class invariant
                classInvariantGenerator.generateInvariantAssertionStatement(type, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                hasClassInvariant = true;
            }
        }

        if (!hasClassInvariant)  {
            hasClassInvariant = classInvariantGenerator.generateDefaultInvariantAssertionMethod(type);
        }
    }

    private void addPreOrPostconditionsToAfterPropertiesSet(ClassNode type) {
        if (!isSpringComponent) return;

        boolean foundAfterPropertiesSet = false;

        for (ClassNode interfaceClassNode : type.getAllInterfaces())  {
            if (interfaceClassNode.getName().equals(INITIALIZINGBEAN_INTERFACE))  {
                foundAfterPropertiesSet = type.getDeclaredMethod(AFTER_PROPERTIES_SET, Parameter.EMPTY_ARRAY) != null;
            }
        }

        if (foundAfterPropertiesSet) return;

        try {
            final Class<?> initializingBeanClass = ContractsVisitor.class.getClassLoader().loadClass(INITIALIZINGBEAN_INTERFACE);
            final ClassNode initializingBeanClassNode = ClassHelper.makeWithoutCaching(initializingBeanClass);

            // add the interface
            type.addInterface(initializingBeanClassNode);

            // add afterPropertiesSet
            final MethodNode afterPropertiesSetMethodNode =
                    type.addMethod(AFTER_PROPERTIES_SET, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, new BlockStatement());

            addPreOrPostcondition(type, afterPropertiesSetMethodNode);

        } catch (ClassNotFoundException e) {
            addError("Could not found class '\"" + INITIALIZINGBEAN_INTERFACE + "\" in classpath!", type);
            return;
        }


    }

    private void addPreOrPostconditionsToDeclaredMethods(ClassNode type) {
        for (final MethodNode methodNode : type.getAllDeclaredMethods())  {
            addPreOrPostcondition(type, methodNode);
        }
    }

    private void addPreOrPostconditionsToConstructors(ClassNode type) {
        // if this is a Spring component class, we'll skip constructor modifications
        if (isSpringComponent) return;

        for (final MethodNode methodNode : type.getDeclaredConstructors())  {
            addPreOrPostcondition(type, methodNode);
        }
    }

    public void addConfigurationVariable(final ClassNode type) {

        MethodCallExpression methodCall = new MethodCallExpression(new ClassExpression(ClassHelper.makeWithoutCaching(Configurator.class)), "checkAssertionsEnabled", new ArgumentListExpression(new ConstantExpression(type.getName())));
        final FieldNode fieldNode = type.addField(GCONTRACTS_ENABLED_VAR, Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, ClassHelper.Boolean_TYPE, methodCall);
        fieldNode.setSynthetic(true);
    }

    public void addPreOrPostcondition(final ClassNode type, final MethodNode method) {

        if (!CandidateChecks.isPreOrPostconditionCandidate(type, method)) return;

        boolean preconditionFound = false;
        boolean postconditionFound = false;

        final ClassInvariantGenerator classInvariantGenerator = new ClassInvariantGenerator(source);
        final PreconditionGenerator preconditionGenerator = new PreconditionGenerator(source);
        final PostconditionGenerator postconditionGenerator = new PostconditionGenerator(source);

        List<AnnotationNode> annotations = method.getAnnotations();
        for (AnnotationNode annotation: annotations)  {
            if (annotation.getClassNode().getName().equals(Requires.class.getName()))  {
                preconditionGenerator.generatePreconditionAssertionStatement(type, method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                preconditionFound = true;
            } else if (annotation.getClassNode().getName().equals(Ensures.class.getName()))  {
                postconditionGenerator.generatePostconditionAssertionStatement(method, (ClosureExpression) annotation.getMember(CLOSURE_ATTRIBUTE_NAME));
                postconditionFound = true;
            }
        }

        // adding a default precondition if an inherited precondition is found
        if (!preconditionFound)  {
            preconditionGenerator.generateDefaultPreconditionStatement(type, method);
        }

        // adding a default postcondition if an inherited postcondition is found
        if (!postconditionFound)  {
            postconditionGenerator.generateDefaultPostconditionStatement(type, method);
        } else {
            // generation the old variables method just in case a postcondition is found
            // in this class
            postconditionGenerator.addOldVariablesMethod(type);
        }

        // If there is a class invariant we will append the check to this invariant
        // after each method call
        if (hasClassInvariant && CandidateChecks.isClassInvariantCandidate(method))  {
            classInvariantGenerator.addInvariantAssertionStatement(type, method);
        }
    }


}
