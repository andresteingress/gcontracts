package org.gcontracts.tests.pre

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * @author ast
 */

class SimplePreconditionTests extends BaseTestClass {

  def source = '''
@EnableAssertions
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
}