package org.gcontracts.compability

/**
 * @author me@andresteingress.com
 */
class EqualsAndHashCodeTests extends GroovyShellTestCase {

    void testEqualsAndHashCode()  {

        def result = evaluate """
            import org.gcontracts.annotations.*

            @Invariant({ name && lastName })
            @groovy.transform.EqualsAndHashCode class Person {
                 String name
                 String lastName

                 def Person(name, lastName)  {
                    this.name = name
                    this.lastName = lastName
                 }
            }
            new Person('Max', 'Mustermann').equals(new Person('Max', 'Mustermann'))
        """

        assertTrue result as boolean
    }
}
