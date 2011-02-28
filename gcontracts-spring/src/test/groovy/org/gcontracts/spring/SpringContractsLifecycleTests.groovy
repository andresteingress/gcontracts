package org.gcontracts.spring

import java.lang.reflect.Method
import junit.framework.TestCase

/**
 * @author ast
 */
class SpringContractsLifecycleTests extends TestCase {

    void test_with_simple_spring_bean()  {

        GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = gcl.parseClass('''
        import org.springframework.stereotype.*
        import org.gcontracts.annotations.*

        @Service
        @Invariant({ someName != null })
        class MyService {
            String someName
        }
        ''')

        assertNotNull clz.methods.find { Method m -> m.name == "\$gcontracts_postConstruct"}
    }
}
