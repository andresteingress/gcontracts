package org.gcontracts.compability

/**
 * @author me@andresteingress.com
 */
class TupleConstructorTests extends GroovyShellTestCase {

    void testTupleConstructor()  {

        evaluate """
        import org.gcontracts.annotations.*

        @Invariant({ firstName && lastName })
        @groovy.transform.TupleConstructor class Person {
            String firstName
            String lastName
        }

        new Person('Max', 'Mustermann')
        """

    }

}
