package org.gcontracts.tests.pre

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author ast
 */

class SimplePreconditionTests extends BaseTestClass {

  def source = '''
package tests

import org.gcontracts.annotations.*

class A {

  def property
  def property2

  @Requires({ value != null })
  def void change_property_value(def value)  {
    property = value
  }

  @Requires({ value != null && value2 != null })
  def void change_property_values(def value, def value2)  {
    property  = value
    property2 = value2
  }

  @Requires({ !(property == value) })
  def void change_property_value_not(def value)  {
    ;
  }
}
'''

  def void test_simple_boolean_expression()  {

    def a = create_instance_of(source)
    a.change_property_value('test')

    shouldFail AssertionError, {
      a.change_property_value(null)
    }
  }

  def void test_binary_boolean_expression()  {

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

  def void test_negated_boolean_expression()  {

    def a = create_instance_of(source)
    a.change_property_value_not('test')
  }
}