package org.groovy.transform.compability

/**
 * @author me@andresteingress.com
 */
class TypedCheckedTests extends GroovyShellTestCase {

    void testTypeCheckedCodeWithAnnotationClosures()  {
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

}
