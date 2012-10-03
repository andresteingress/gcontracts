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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Handles {@code -ea} and {@code -da} runtime input arguments for enabling and
 * disabling contract elements.</p>
 *
 * @author ast
 */
public final class Configurator {

    public static final String DISABLED_ASSERTIONS = "-da";
    public static final String ENABLED_ASSERTIONS = "-ea";

    public static final String PACKAGE_PREFIX = ":";
    public static final String ENABLE_PACKAGE_ASSERTIONS = ENABLED_ASSERTIONS + PACKAGE_PREFIX;
    public static final String DISABLE_PACKAGE_ASSERTIONS = DISABLED_ASSERTIONS + PACKAGE_PREFIX;
    public static final String PACKAGE_POSTFIX = "...";
    
    private static Map<String, Boolean> assertionConfiguration;

    static {
        initAssertionConfiguration();
    }

    private static void initAssertionConfiguration()  {

        assertionConfiguration = new HashMap<String, Boolean>();
        // per default assertion are enabled (Groovy like)
        assertionConfiguration.put(null, Boolean.TRUE);

        RuntimeMXBean runtimemxBean = ManagementFactory.getRuntimeMXBean();
        for (String arg : runtimemxBean.getInputArguments())  {
            if (DISABLED_ASSERTIONS.equals(arg))  {
                assertionConfiguration.put(null, Boolean.FALSE);

            } else if (arg.startsWith(ENABLE_PACKAGE_ASSERTIONS) && arg.endsWith(PACKAGE_POSTFIX))  {
                final String packageName = arg.substring(ENABLE_PACKAGE_ASSERTIONS.length(), arg.length() - PACKAGE_POSTFIX.length());
                assertionConfiguration.put(packageName, Boolean.TRUE);

            } else if (arg.startsWith(DISABLE_PACKAGE_ASSERTIONS) && arg.endsWith(PACKAGE_POSTFIX))  {
                final String packageName = arg.substring(DISABLE_PACKAGE_ASSERTIONS.length(), arg.length() - PACKAGE_POSTFIX.length());

                assertionConfiguration.put(packageName, Boolean.FALSE);
            } else if (arg.startsWith(ENABLE_PACKAGE_ASSERTIONS))  {
                final String className = arg.substring(ENABLE_PACKAGE_ASSERTIONS.length(), arg.length());
                assertionConfiguration.put(className, Boolean.TRUE);

            } else if (arg.startsWith(DISABLE_PACKAGE_ASSERTIONS))  {
                final String className = arg.substring(DISABLE_PACKAGE_ASSERTIONS.length(), arg.length());

                assertionConfiguration.put(className, Boolean.FALSE);
            }
        }
    }

    /**
     * This static method is used within generated code to check whether assertions have been disabled for the current class or not.
     *
     * @param className the class name to look up in the assertion configuration
     * @return whether assertion checking is enabled or not
     */
    public static boolean checkAssertionsEnabled(final String className)  {
        return internalMethod(className);
    }

    private static boolean internalMethod(String className) {
        if (className == null || className.length() == 0) return false;

        if (assertionConfiguration.containsKey(className)) return assertionConfiguration.get(className);
        if (className.lastIndexOf('.') < 0) return assertionConfiguration.get(null);

        String packageName = className.substring(0, className.lastIndexOf('.'));

        while (!assertionConfiguration.containsKey(packageName))  {
            int dotIndex = packageName.lastIndexOf('.');
            if (dotIndex < 0) return assertionConfiguration.get(null);

            packageName = packageName.substring(0, dotIndex);
        }

        if (assertionConfiguration.containsKey(packageName)) return assertionConfiguration.get(packageName);

        return assertionConfiguration.get(null);
    }
}
