/**
 * Copyright (c) 2010, gcontracts.lib@gmail.com
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
package org.gcontracts.generation;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * <p>
 * Base class for GContracts code generators.
 * </p>
 *
 * @author andre.steingress@gmail.com
 */
public abstract class BaseGenerator {

    public static final String INVARIANT_CLOSURE_PREFIX = "invariant";

    protected final ReaderSource source;

    public BaseGenerator(final ReaderSource source)  {
        this.source = source;
    }                                     

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the field name of the invariant closure field of the given <tt>classNode</tt>
     */
    public static String getInvariantMethodName(final ClassNode classNode)  {
        return INVARIANT_CLOSURE_PREFIX + "_" + classNode.getName().replaceAll("\\.", "_");
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the {@link org.codehaus.groovy.ast.MethodNode} which contains the invariant of the given <tt>classNode</tt>
     */
    public static MethodNode getInvariantMethodNode(final ClassNode classNode)  {
        return classNode.getDeclaredMethod(getInvariantMethodName(classNode), Parameter.EMPTY_ARRAY);
    }
}
