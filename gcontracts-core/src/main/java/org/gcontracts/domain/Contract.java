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
package org.gcontracts.domain;

import org.codehaus.groovy.ast.ClassNode;
import org.gcontracts.util.Validate;

/**
 * <p>Represents a contract between a supplier and a customer of a class.</p>
 *
 * @author ast
 */
public class Contract {

    private final ClassNode classNode;

    private ClassInvariant classInvariant = ClassInvariant.DEFAULT;
    private AssertionMap<Precondition> preconditions;
    private AssertionMap<Postcondition> postconditions;

    public Contract(final ClassNode classNode)  {
        Validate.notNull(classNode);

        this.classNode = classNode;
        this.preconditions = new AssertionMap<Precondition>();
        this.postconditions = new AssertionMap<Postcondition>();
    }

    public ClassNode classNode() { return classNode; }

    public void setClassInvariant(final ClassInvariant classInvariant)  {
        Validate.notNull(classInvariant);
        this.classInvariant = classInvariant;
    }

    public AssertionMap<Precondition> preconditions() { return preconditions; }
    public AssertionMap<Postcondition> postconditions() { return postconditions; }

    public boolean hasDefaultClassInvariant() { return classInvariant == ClassInvariant.DEFAULT; }
    public ClassInvariant classInvariant() { return classInvariant; }
}
