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
package org.gcontracts.generation;

import groovy.lang.Interceptor;
import org.gcontracts.CyclicAssertionCallException;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link Interceptor} implementation which checks for cyclic method calls - as these need to be avoided
 * in contract definitions.
 *
 * @see Interceptor
 *
 * @author ast
 */
public class CyclicAssertionCallInterceptor implements Interceptor {

    private List<String> callStack = new LinkedList<String>();

    @Override
    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        if (callStack.contains(methodName)) throw new CyclicAssertionCallException("Method '" + methodName + "' has already been called from the current assertion - assertion call cycle detected!");
        callStack.add(methodName);
        System.out.println("ADDED " + methodName);
        return object;
    }

    @Override
    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        callStack.remove(methodName);
        System.out.println("REMOVED " + methodName);
        return result;
    }

    @Override
    public boolean doInvoke() {
        return true;
    }
}