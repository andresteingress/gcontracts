package org.groovy.transform.compability

/**
 * @author me@andresteingress.com
 */
class TypeCheckedTests extends GroovyShellTestCase {

    void testPrecondition()  {
        evaluate """
            import org.gcontracts.annotations.*

            @groovy.transform.TypeChecked
            class A {

                @Requires({ some?.size() > 0 })
                def op(String some) {

                }
            }

            def a = new A()
        """
    }

    void testPostcondition()  {
        evaluate """
            import org.gcontracts.annotations.*

            @groovy.transform.TypeChecked
            class A {

                @Ensures({ result.size() > 0 })
                def op(String some) {
                    some
                }
            }

            def a = new A()
        """

        evaluate """
            import org.gcontracts.annotations.*

            @groovy.transform.TypeChecked
            class A {

                private int i = 12

                @Ensures({ old.i == 12 })
                def op(String some) {
                    some
                }
            }

            def a = new A()
        """
    }

    void testClassInvariant()  {
        evaluate """
            import org.gcontracts.annotations.*

            @groovy.transform.TypeChecked
            @Invariant({ i >= 0 })
            class A {
                private int i = 12
            }

            def a = new A()
        """
    }

}
