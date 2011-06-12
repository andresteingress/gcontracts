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

  @Test void no_postcondition_on_static_methods()  {

    def source = """
        import org.gcontracts.annotations.*

class Account {

   @Ensures({ amount != null })
   static def withdraw(def amount)  {
     return amount
   }
}
    """

    def clazz = add_class_to_classpath(source)
    assert clazz.withdraw(null) == null
  }

    @Test void use_result_with_parameter_value()  {


        def source = """

        import org.gcontracts.annotations.*

        class A {

            @Ensures({ result.size() == 2 && result.contains(s) && result.contains(s2) })
            List<String> toList(String s, String s2)  {
               [s, s2].sort()
            }

        }
        """

        def a = create_instance_of(source)
        a.toList("a", "b") == ["a", "b"]

    }

    @Test void complex_return_statement()  {

        def source = """
        import org.gcontracts.annotations.*

        class A {

            @Requires({ ( messages != null ) })
            @Ensures({  ( result   != null ) && ( result.size() == messages.size()) })
            List<String> sortForAll ( Collection<String> messages )
            {
                messages.sort {
                    String m1, String m2 ->

                    int  urgencyCompare = ( m1 <=> m2 )
                    if ( urgencyCompare != 0 ){ return urgencyCompare }

                    - ( m1 <=> m2 )
                }
            }

        }

        """

        def a = create_instance_of(source)
        a.sortForAll(["test1", "test2"]) == ["test1", "test2"]

    }
}
