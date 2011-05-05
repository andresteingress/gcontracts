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

import groovy.lang.GroovyObject;
import groovy.lang.Interceptor;
import groovy.lang.MetaMethod;

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
public class CircularAssertionCallInterceptor implements Interceptor {

    private List<MetaMethod> callStack = new LinkedList<MetaMethod>();
    private boolean methodInvoke = true;

    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        MetaMethod metaMethod = ((GroovyObject) object).getMetaClass().getMetaMethod(methodName, arguments);

        if (callStack.contains(metaMethod))  {
            methodInvoke = false;
            return true;
        }

        callStack.add(metaMethod);

        return object;
    }

    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        MetaMethod metaMethod = ((GroovyObject) object).getMetaClass().getMetaMethod(methodName, arguments);
        callStack.remove(metaMethod);
        methodInvoke = true;

        return result;
    }

    public boolean doInvoke() {
        return methodInvoke;
    }
}