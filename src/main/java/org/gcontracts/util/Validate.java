package org.gcontracts.util;

/**
 * @author andre.steingress@gmail.com
 */
public class Validate {

    public static void notNull(Object obj) {
        if (obj == null) throw new AssertionError("obj must not be null");
    }

    public static void isTrue(boolean expression)  {
        if (!expression) throw new AssertionError("expression must be true");
    }

}
