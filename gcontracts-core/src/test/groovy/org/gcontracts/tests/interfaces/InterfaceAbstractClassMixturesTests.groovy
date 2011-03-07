package org.gcontracts.tests.interfaces

import org.gcontracts.PostconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test
import org.gcontracts.PreconditionViolation

/**
 * @author ast
 */
class InterfaceAbstractClassMixturesTests extends BaseTestClass {

    @Test void class_with_abstract_class_and_interface() {

        def s1 = '''
        @AssertionsEnabled
        package tests

        import org.gcontracts.annotations.*

        interface Stackable {
            @Ensures({ result != null })
            def pop()
        }

        abstract class StackableAbstract implements Stackable {
            abstract def pop()
        }
        '''

        def s2 = '''
        @AssertionsEnabled
        package tests

        import org.gcontracts.annotations.*

        class Stack extends StackableAbstract {
            def pop() { return null }
        }
        '''

        add_class_to_classpath(s1)
        def stack = create_instance_of(s2)

        shouldFail PostconditionViolation.class, {
            stack.pop()
        }
    }

    @Test void interface_and_abstract_class_both_contain_abstract_methods() {

        def s1 = '''
        @AssertionsEnabled
        package tests

        import org.gcontracts.annotations.*

        interface Stackable {
            @Ensures({ result != null })
            def pop()
        }

        abstract class StackableAbstract implements Stackable {
            @Requires({ item != null })
            abstract def push(def item)
        }
        '''

        def s2 = '''
        @AssertionsEnabled
        package tests

        import org.gcontracts.annotations.*

        class Stack extends StackableAbstract {
            def pop() { return null }
            def push(def item) {}
        }
        '''

        add_class_to_classpath(s1)
        def stack = create_instance_of(s2)

        shouldFail PostconditionViolation.class, {
            stack.pop()
        }

        shouldFail PreconditionViolation.class, {
            stack.push(null)
        }
    }
}
