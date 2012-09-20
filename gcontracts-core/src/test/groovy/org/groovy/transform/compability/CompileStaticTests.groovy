package org.groovy.transform.compability

/**
 * @author me@andresteingress.com
 */
class CompileStaticTests extends GroovyShellTestCase {
    
    void testPrecondition()  {
        evaluate """
            import org.gcontracts.annotations.*

            @groovy.transform.CompileStatic
            class A {

                @Requires({ param.size() > 0 })
                void someOperation(String param) { }
            }
            new A()
        """
    }

    void testPostcondition()  {
        evaluate """
                import org.gcontracts.annotations.*

                @groovy.transform.CompileStatic
                class A {

                    @Ensures({ result == 3  })
                    Integer add() { return 1 + 1 }
                }
                new A()
            """
    }

    void testClassInvariant()  {
        evaluate """
            import org.gcontracts.annotations.*

            @groovy.transform.CompileStatic
            @Invariant({ speed >= 0 })
            class A {
                Integer speed = 1
            }
            new A()
        """
    }
    
}
