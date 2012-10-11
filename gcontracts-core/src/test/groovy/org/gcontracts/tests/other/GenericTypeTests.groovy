package org.gcontracts.tests.other

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * User: asteingress
 * Date: 10/11/12
 */
class GenericTypeTests extends BaseTestClass {

    @Test
    void requires_on_generic_type_parameter() {

        def source = """
           import org.gcontracts.annotations.*

           class A {

              @Requires({ ref != null })
              public <T> T m(T ref) { ref }
           }

        """
        def a = create_instance_of(source)
        assert a.m('test') == 'test'
    }

    @Test
    void ensures_on_generic_type_parameter() {

        def source = """
               import org.gcontracts.annotations.*

               class A {

                  @Ensures({ result != null })
                  public <T> T m(T ref) { ref }
               }

            """
        def a = create_instance_of(source)
        assert a.m('test') == 'test'
    }

    @Test
    void invariant_on_generic_type_parameter() {

        def source = """
               import org.gcontracts.annotations.*

               @groovy.transform.CompileStatic
               @Invariant({ property.size() >= 0 })
               class A<T extends java.util.List> {

                  T property = []


                  @Ensures({ result != null })
                  public <T> T m(T ref) { ref }
               }

        """
        def a = create_instance_of(source)
        assert a.m('test') == 'test'
    }
}
