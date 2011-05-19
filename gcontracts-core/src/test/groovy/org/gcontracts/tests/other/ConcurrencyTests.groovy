package org.gcontracts.tests.other

import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * @author andre.steingress@gmail.com
 */
class ConcurrencyTests extends BaseTestClass {

    @Test void preconditionWithTimeout() {

        def source = '''
        package test

        import org.gcontracts.annotations.*

        class A {

            def sleepNowCalled = false
            def instance = 1

            @Requires({ instance == 1 && sleepNow(test) })
            void m1(def test) {
                println 'calling m1'
                assert instance == 1
                instance = 2
            }

            def sleepNow(def millis)  {
                println "calling sleepNow with $millis"

                if (sleepNowCalled) return true

                sleep (millis)
                sleepNowCalled = true

                return true
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        def a = clazz.newInstance()

        def t1 = Thread.start {
            println 't1 entered'
            a.m1 5000
            println 't1 exited'
        }

        boolean preconditionOccured = false

        // wait some time to let the first thread startup
        sleep(500)

        def t2 = Thread.start {
            println 't2 entered'

            try {
                a.m1 500
            } catch (PreconditionViolation pv) {
                preconditionOccured = true
            }

            println 't2 exited'
        }


        [t1, t2]*.join()

        assert preconditionOccured

    }
}
