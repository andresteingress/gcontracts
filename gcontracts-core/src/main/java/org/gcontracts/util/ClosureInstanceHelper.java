package org.gcontracts.util;

import groovy.lang.Closure;
import org.codehaus.groovy.GroovyBugError;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Used to instantiate closure classes used by GContracts annotations.
 *
 * User: asteingress
 * Date: 10/2/12
 */
public class ClosureInstanceHelper {

    public static Closure createInstance(Class<Closure> closureClass, Object[] arguments)  {

        Constructor<?>[] constructors = closureClass.getConstructors();
        if (constructors == null || constructors.length > 1) return null;

        Constructor<?> constructor = constructors[0];
        try {
            return (Closure) constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new GroovyBugError("Contract closure could not be instantiated. This indicates a bug in GContracts, please file an issue!", e);
        }
    }
}
