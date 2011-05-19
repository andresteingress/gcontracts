package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.*
import org.gcontracts.PostconditionViolation;

/**
 * @author ast
 */
class SimplePostconditionTests extends BaseTestClass {

  def source_postconditions = '''
@Contracted
package tests

import org.gcontracts.annotations.*

class A {

  def property
  def property2

  @Ensures({ property == value })
  void change_property_value(def value)  {
    property = value
  }

  @Ensures({ property == value && property2 == value2 })
  void change_property_values(def value, def value2)  {
    property  = value
    property2 = value2
  }

  @Ensures({ !(property == value) })
  void change_property_value_not(def value)  {
    ;
  }
}
'''

  @Test void simple_boolean_expression()  {

    def a = create_instance_of(source_postconditions)
    a.change_property_value('test')

    assertEquals 'test', a.property
  }

  @Test void binary_boolean_expression()  {

    def a = create_instance_of(source_postconditions)
    a.change_property_values('test', 'test2')

    assertEquals 'test', a.property
    assertEquals 'test2', a.property2
  }

  @Test void negated_boolean_expression()  {

    def a = create_instance_of(source_postconditions)
    a.change_property_value_not('test')
  }


  @Test void multiple_return_statements()  {

    def source = """
        import org.gcontracts.annotations.*

class Account {

   @Ensures({ result == 2 })
   def some_method()  {
     if (true)  {
         return 1
     }

     return 2
   }
}
    """

    def a = create_instance_of(source)
    shouldFail PostconditionViolation, {
      a.some_method()
    }
  }

    @Test void multiple_return_statements_with_try_finally()  {

    def source = """
        import org.gcontracts.annotations.*

class Account {

   @Ensures({ result == 3 })
   def some_method()  {
     if (true)  {
         try {
            throw new Exception ('test')
            return 1
         } finally {
            return 3
         }
     }

     return 2
   }
}
    """

    def a = create_instance_of(source)
    assert a.some_method()
  }

  @Test void multiple_return_statements_with_try_finally_violation()  {

    def source = """
        import org.gcontracts.annotations.*

class Account {

   @Ensures({ result != 3 })
   def some_method()  {
     if (true)  {
         try {
            throw new Exception ('test')
            return 1
         } finally {
            return 3
         }
     }

     return 2
   }
}
    """

    def a = create_instance_of(source)
    shouldFail PostconditionViolation, {
      a.some_method()
    }
  }
}
