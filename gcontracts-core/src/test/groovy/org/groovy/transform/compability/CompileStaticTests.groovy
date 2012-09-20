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
            def a = new A()
        """
        
    }

    void testPostcondition()  {
        evaluate """
                import org.gcontracts.annotations.*

                @groovy.transform.CompileStatic
                class A {

                    @Ensures({ false  })
                    Integer add() { return 1 + 1 }
                }
                def a = new A()
                a.add()
            """

    }
    
}
