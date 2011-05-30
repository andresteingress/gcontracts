package org.gcontracts.tests.pre

import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

 /**
 * @author ast
 */

class SimplePreconditionTests extends BaseTestClass {

  def source = '''
@Contracted
package tests

import org.gcontracts.annotations.*

class A {

  def property
  def property2

  @Requires({ value != null })
  void change_property_value(def value)  {
    property = value
  }

  @Requires({ value != null && value2 != null })
  void change_property_values(def value, def value2)  {
    property  = value
    property2 = value2
  }

  @Requires({ !(property == value) })
  void change_property_value_not(def value)  {
    ;
  }
}
'''

  @Test void simple_boolean_expression()  {

    def a = create_instance_of(source)
    a.change_property_value('test')

    shouldFail AssertionError, {
      a.change_property_value(null)
    }
  }

  @Test void binary_boolean_expression()  {

    def a = create_instance_of(source)
    a.change_property_values('test', 'test2')

    shouldFail AssertionError, {
      a.change_property_values(null, 'test2')
    }

    shouldFail AssertionError, {
      a.change_property_values('test1', null)
    }

    shouldFail AssertionError, {
      a.change_property_values(null, null)
    }
  }

  @Test void negated_boolean_expression()  {

    def a = create_instance_of(source)
    a.change_property_value_not('test')
  }

  @Test void precondition_in_constructor_declaration()  {

    def source = """
import org.gcontracts.annotations.*

class Account
{
    protected BigDecimal balance

    @Requires({ amount >= 0.0 })
    def Account( BigDecimal amount = 0.0 )
    {
        balance = amount
    }
}
    """
    shouldFail PreconditionViolation, {
        create_instance_of(source, [-10.0])
    }
  }

  @Test void precondition_in_private_constructor_declaration()  {

    def source = """
import org.gcontracts.annotations.*

class Account
{
    protected BigDecimal balance

    @Requires({ amount >= 0.0 })
    private def Account( BigDecimal amount = 0.0 )
    {
        balance = amount
    }
}
    """
    shouldFail PreconditionViolation, {
        create_instance_of(source, [-10.0])
    }
  }

  @Test void precondition_in_private_method()  {

    def source = """
import org.gcontracts.annotations.*

class Account
{

    @Requires({ amount != null })
    private def withdraw(def amount) { println amount }
}
    """
    shouldFail PreconditionViolation, {
        def account = create_instance_of(source)
        account.withdraw(null)
    }
  }

  @Test void recursive_preconditions()  {

    def source = """
import org.gcontracts.annotations.*

class Account
{

    @Requires({ amount != null })
    private def withdraw(def amount) { if (amount < 0) return 0 else withdraw (amount - 10) }
}
    """
    def account = create_instance_of(source)
    account.withdraw(10)
  }

  @Test void sueer_precondition_call_should_be_done()  {

    def accountClassSource = """
import org.gcontracts.annotations.*

class Account
{

    @Requires({ amount != null })
    def withdraw(def amount) { if (amount < 0) return 0 else withdraw (amount - 10) }
}
    """
    def descendantAccountClassSource = """
import org.gcontracts.annotations.*

class BetterAccount extends Account
{

    @Requires({ true })
    def withdraw(def amount) { super.withdraw(amount) }
}
    """

    add_class_to_classpath accountClassSource

    def account = create_instance_of(descendantAccountClassSource)

    shouldFail PreconditionViolation, {
        account.withdraw(null)
    }
  }

  @Test void precondition_in_static_method()  {

    def source = """
import org.gcontracts.annotations.*

class Account
{
    @Requires({ amount >= 0.0 })
    static void withdraw( BigDecimal amount = 0.0 )
    {
        println amount
    }
}
    """
    shouldFail PreconditionViolation, {
        def clazz = add_class_to_classpath(source)
        clazz.withdraw(null)
    }
  }

  @Test void precondition_in_factory_method()  {

      def source = """
        import org.gcontracts.annotations.*

        class Factory {
        @Requires({ g != null })
        static void create(def g) {
            println g
        }
        }
     """

      shouldFail PreconditionViolation, {
        def clazz = add_class_to_classpath(source)
        clazz.create(null)
      }

  }
}