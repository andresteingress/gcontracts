package org.gcontracts.compability

/**
 * User: asteingress
 * Date: 10/15/12
 */
class SynchronizedTests extends GroovyShellTestCase {

    void test_Synchronized_on_methods()  {

        def source = """
            import org.gcontracts.annotations.*

            class A {

                @groovy.transform.Synchronized
                @Requires({ a >= 0 })
                def m(int a) { return a}

            }

            def a = new A()
            a.m(12)
        """

        evaluate source
    }
}
