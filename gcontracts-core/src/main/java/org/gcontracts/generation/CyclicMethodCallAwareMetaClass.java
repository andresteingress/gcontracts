/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gcontracts.generation;

import groovy.lang.*;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CyclicMethodCallAwareMetaClass extends MetaClassImpl implements AdaptingMetaClass {

    protected MetaClass adaptee = null;
    protected MetaClass origMetaClass = null;
    protected Interceptor interceptor = null;

    private static Map<String, CyclicMethodCallAwareMetaClass> metaClasses = new HashMap<String, CyclicMethodCallAwareMetaClass>();

    /**
     * convenience factory method for the most usual case.
     */
    public synchronized static CyclicMethodCallAwareMetaClass getInstance(Class theClass) throws IntrospectionException {

        if (metaClasses.containsKey(theClass.getName())) return metaClasses.get(theClass.getName());

        MetaClassRegistry metaRegistry = GroovySystem.getMetaClassRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);

        metaClasses.put(theClass.getName(), new CyclicMethodCallAwareMetaClass(metaRegistry, theClass, meta));

        return metaClasses.get(theClass.getName());
    }

    /**
     * @param adaptee the MetaClass to decorate with interceptability
     */
    public CyclicMethodCallAwareMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) throws IntrospectionException {
        super(registry, theClass);
        this.adaptee = adaptee;
        if (null == adaptee) throw new IllegalArgumentException("adaptee must not be null");
        super.initialize();
    }

    public synchronized void initialize() {
        this.adaptee.initialize();
    }

    private void init() {
        // grab existing meta (usually adaptee but we may have nested use calls)
        origMetaClass = registry.getMetaClass(theClass);
        registry.setMetaClass(theClass, this);
    }

    public void deinit()  {
        if (origMetaClass != null) registry.setMetaClass(theClass, origMetaClass);
        origMetaClass = null;
    }

    /**
     * @return the interceptor in use or null if no interceptor is used
     */
    public Interceptor getInterceptor() {
        return interceptor;
    }

    /**
     * @param interceptor may be null to reset any interception
     */
    public void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    /**
     * Call invokeMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeMethod(object, methodName, arguments);
            }
        });
    }

    /**
     * Call invokeStaticMethod on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeStaticMethod(final Object object, final String methodName, final Object[] arguments) {
        return doCall(object, methodName, arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeStaticMethod(object, methodName, arguments);
            }
        });
    }

    /**
     * Call invokeConstructor on adaptee with logic like in MetaClass unless we have an Interceptor.
     * With Interceptor the call is nested in its beforeInvoke and afterInvoke methods.
     * The method call is suppressed if Interceptor.doInvoke() returns false.
     * See Interceptor for details.
     */
    public Object invokeConstructor(final Object[] arguments) {
        return doCall(theClass, "ctor", arguments, interceptor, new Callable() {
            public Object call() {
                return adaptee.invokeConstructor(arguments);
            }
        });
    }

    /**
     * Interceptors the call to getProperty if a PropertyAccessInterceptor is
     * available
     *
     * @param object   the object to invoke the getter on
     * @param property the property name
     * @return the value of the property
     */
    public Object getProperty(Class aClass, Object object, String property, boolean b, boolean b1) {
        if (null == interceptor) {
            return super.getProperty(aClass, object, property, b, b1);
        }
        if (interceptor instanceof PropertyAccessInterceptor) {
            PropertyAccessInterceptor pae = (PropertyAccessInterceptor) interceptor;

            Object result = pae.beforeGet(object, property);
            if (interceptor.doInvoke()) {
                result = super.getProperty(aClass, object, property, b, b1);
            }
            return result;
        }
        return super.getProperty(aClass, object, property, b, b1);
    }

    /**
     * Interceptors the call to a property setter if a PropertyAccessInterceptor
     * is available
     *
     * @param object   The object to invoke the setter on
     * @param property The property name to set
     * @param newValue The new value of the property
     */
    public void setProperty(Class aClass, Object object, String property, Object newValue, boolean b, boolean b1) {
        if (null == interceptor) {
            super.setProperty(aClass, object, property, newValue, b, b1);
        }
        if (interceptor instanceof PropertyAccessInterceptor) {
            PropertyAccessInterceptor pae = (PropertyAccessInterceptor) interceptor;

            pae.beforeSet(object, property, newValue);
            if (interceptor.doInvoke()) {
                super.setProperty(aClass, object, property, newValue, b, b1);
            }
        } else {
            super.setProperty(aClass, object, property, newValue, b, b1);
        }
    }

    public MetaClass getAdaptee() {
        return this.adaptee;
    }

    public void setAdaptee(MetaClass metaClass) {
        this.adaptee = metaClass;
    }

    // since Java has no Closures...
    private interface Callable {
        Object call();
    }

    private Object doCall(Object object, String methodName, Object[] arguments, Interceptor interceptor, Callable howToInvoke) {
        if (null == interceptor) {
            return howToInvoke.call();
        }
        Object result = interceptor.beforeInvoke(object, methodName, arguments);
        if (interceptor.doInvoke()) {
            result = howToInvoke.call();
        }
        result = interceptor.afterInvoke(object, methodName, arguments, result);
        return result;
    }
}
