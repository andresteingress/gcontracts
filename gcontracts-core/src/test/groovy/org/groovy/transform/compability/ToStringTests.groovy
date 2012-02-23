package org.groovy.transform.compability

/**
 * @author me@andresteingress.com
 */
class ToStringTests extends GroovyShellTestCase {

    void testToString()  {

        def result = evaluate """
           import org.gcontracts.annotations.*

           @Invariant({ name })
           @groovy.transform.ToString
           class Person {
               String name

               def Person(String name)  {
                  this.name = name
               }
           }

           new Person('Max Mustermann').toString()
        """

        assertEquals result, 'Person(Max Mustermann)'

    }
}
