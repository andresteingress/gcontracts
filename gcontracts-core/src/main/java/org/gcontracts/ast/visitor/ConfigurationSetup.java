/**
 * Copyright (c) 2011, Andre Steingress
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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.generation.Configurator;
import org.gcontracts.util.Validate;
import org.objectweb.asm.Opcodes;

/**
 * Makes some initialization in order to use the {@link Configurator} for determining
 * which assertions in what packages will be executed.
 *
 * @see Configurator
 *
 * @author ast
 */
public class ConfigurationSetup {

    /**
     * Adds an instance field which allows to control whether GContract assertions
     * are enabled or not. Before assertions are evaluated this field will be checked.
     *
     * @see Configurator
     *
     * @param type the current {@link ClassNode}
     */
    public void init(final ClassNode type) {
        Validate.notNull(type);

        StaticMethodCallExpression checkAssertionsEnabledMethodCall = new StaticMethodCallExpression(ClassHelper.makeWithoutCaching(Configurator.class), "checkAssertionsEnabled", new ArgumentListExpression(new ConstantExpression(type.getName())));

        final FieldNode fieldNode = type.addField(BaseVisitor.GCONTRACTS_ENABLED_VAR, Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, ClassHelper.boolean_TYPE, checkAssertionsEnabledMethodCall);

        fieldNode.setSynthetic(true);
    }
}
