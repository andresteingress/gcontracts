package org.gcontracts.console

/**
 * User: asteingress
 * Date: 10/12/12
 */
class GroovyConsoleTests extends GroovyTestCase {

    void test_Grab_GContracts_import() {

        GroovyShell shell = new GroovyShell(getClass().getClassLoader())
        shell.evaluate('''
            @Grab('org.gcontracts:gcontracts-core:1.2.10')
            import org.gcontracts.annotations.*

            @groovy.transform.CompileStatic
            class A {

                A() {}

                protected void invariant_A() {}
                protected java.util.Map $_gc_computeOldVariables() { return [:] }
            }

            new A()
        ''')
    }
}
