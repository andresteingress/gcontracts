package org.gcontracts.compability

/**
 * @author me@andresteingress.com
 */
class ImmutableTests extends GroovyShellTestCase {

    void testSimpleImmutableClass()  {

        evaluate """
           import org.gcontracts.annotations.*

           @groovy.transform.Immutable
           @Invariant({ name })
           class Person {
                String name
           }

           def p = new Person(name: 'John Doe')
        """

    }
}
