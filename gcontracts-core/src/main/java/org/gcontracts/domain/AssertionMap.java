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

import org.codehaus.groovy.ast.MethodNode;
import org.gcontracts.util.Validate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ast
 */
public class AssertionMap<T extends Assertion<T>> implements Iterable<Map.Entry<MethodNode, T>> {

    private final Map<MethodNode, T> internalMap;

    public AssertionMap() {
        this.internalMap = new HashMap<MethodNode, T>();
    }

    public void and(final MethodNode methodNode, final T assertion)  {
        Validate.notNull(methodNode);
        Validate.notNull(assertion);

        if (!internalMap.containsKey(methodNode))  {
            internalMap.put(methodNode, assertion);
        } else {
            internalMap.get(methodNode).and(assertion);
        }
    }

    public void or(final MethodNode methodNode, final T assertion)  {
        Validate.notNull(methodNode);
        Validate.notNull(assertion);

        if (!internalMap.containsKey(methodNode))  {
            internalMap.put(methodNode, assertion);
        } else {
            internalMap.get(methodNode).or(assertion);
        }
    }

    public void join(final MethodNode methodNode, final T assertion)  {
        and(methodNode, assertion);
    }

    public boolean contains(final MethodNode methodNode)  {
        return internalMap.containsKey(methodNode);
    }

    public Iterator<Map.Entry<MethodNode, T>> iterator() {
        return internalMap.entrySet().iterator();
    }

    public int size()  {
        return internalMap.size();
    }

    public T get(final MethodNode methodNode)  {
        return internalMap.get(methodNode);
    }
}
