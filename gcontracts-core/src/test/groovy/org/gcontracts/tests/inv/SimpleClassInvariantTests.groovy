package org.gcontracts.tests.inv

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test
import org.gcontracts.ClassInvariantViolation

/**
 * @author ast
 */
class SimpleClassInvariantTests extends BaseTestClass {

    def source1 = '''
@Contracted
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {

  def property

  def A(def someValue)  {
    property = someValue
  }
}
'''

    def source2 = '''
@Contracted
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }
}
'''

    def source3 = '''
@Contracted
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }

  static me = "me"
}
'''

    @Test void class_invariant()  {
        create_instance_of(source1, ['test'])

        shouldFail AssertionError, {
            create_instance_of(source1, [null])
        }
    }

    @Test void class_invariant_with_private_instance_variable()  {
        create_instance_of(source2, ['test'])

        shouldFail AssertionError, {
            create_instance_of(source2, [null])
        }
    }

    @Test void class_with_constant()  {
        create_instance_of(source3, ['test'])
    }


    @Test void multiple_return_statements()  {

        def source = """
        import org.gcontracts.annotations.*

@Invariant({ property != 0 })
class Account {

   def property = 1

   def some_method()  {
     if (true)  {
         property = 0
         return;
     }

     return;
   }
}
    """

        def source2 = """
        import org.gcontracts.annotations.*

@Invariant({ property != 0 })
class Account {

   def property = 1

   def some_method()  {
     if (false)  {
         property = 1
         return;
     }

     property = 0
     return;
   }
}
    """

        def a = create_instance_of(source2)
        shouldFail ClassInvariantViolation, {
            a.some_method()
        }
    }

    @Test void duplicate_return_statements()  {

        def source = """
        import org.gcontracts.annotations.*

@Invariant({ elements != null })
class Stack {

   def elements = []

   def push(def item) {
      elements << item
   }

   def pop()  {
      elements.pop()
   }
}
    """

        def stack = create_instance_of(source)

        stack.push(1)
        stack.push(2)

        assert stack.pop() == 2
        assert stack.pop() == 1
    }

    @Test void avoid_invariant_on_read_only_methods()  {

        def source = """
import org.gcontracts.annotations.*

@Invariant({ speed() >= 0.0 })
class Rocket {

    def speed() { 1.0 }
}

    """

        create_instance_of(source)
    }


    @Test void recursive_invariant_with_getter_method()  {

        def source = """
    import org.gcontracts.annotations.*

    @Invariant({ speed >= 0.0 })
    class Rocket {

        @Requires({ true })
        def getSpeed() { 1.0 }
    }

        """

        create_instance_of(source)
    }

    @Test void direct_field_access()  {

        def source = """
        import org.gcontracts.annotations.*

        @Invariant({ speed >= 0.0 })
        class Rocket {
            def speed = 0.0

            def increase() {
                this.speed -= 1
            }
        }

            """

        def rocket = create_instance_of(source)

        shouldFail (ClassInvariantViolation) {
            rocket.increase()
        }
    }

    @Test void direct_field_access_in_class_invariant()  {

        add_class_to_classpath """
            import org.gcontracts.annotations.*

            @Invariant({ this.speed >= 0.0 })
            class Rocket {
                def speed = 0.0

                def increase() {
                    this.speed -= 1
                }
            }"""
    }

    @Test void private_field_access_in_direct_class()  {

        def c = add_class_to_classpath """
                import org.gcontracts.annotations.*

                @Invariant({ speed >= 0.0 })
                class Rocket {
                    private double speed = 0.0

                    def increase() {
                        this.speed -= 1
                    }
                }"""

        def rocket = c.newInstance()

        shouldFail ClassInvariantViolation, {
            rocket.increase()
        }
    }

    @Test void private_field_access_in_descendant_class()  {

        def c = add_class_to_classpath """
                    import org.gcontracts.annotations.*

                    @Invariant({ speed >= 0.0 })
                    class Rocket {
                        private double speed = 0.0

                        def increase() {
                            this.speed -= 1
                        }
                    }
                    """

        def c2 = add_class_to_classpath """
            class BetterRocket extends Rocket {}
        """

        def betterRocket = c2.newInstance()

        shouldFail ClassInvariantViolation, {
            betterRocket.increase()
        }
    }
}
