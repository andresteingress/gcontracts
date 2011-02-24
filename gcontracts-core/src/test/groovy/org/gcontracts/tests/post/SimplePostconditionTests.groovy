package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author andre.steingress@gmail.com
 */
class SimplePostconditionTests extends BaseTestClass {

  def source_postconditions = '''
package tests

import org.gcontracts.annotations.*

class A {

  def property
  def property2

  @Ensures({ property == value })
  def void change_property_value(def value)  {
    property = value
  }

  @Ensures({ property == value && property2 == value2 })
  def void change_property_values(def value, def value2)  {
    property  = value
    property2 = value2
  }

  @Ensures({ !(property == value) })
  def void change_property_value_not(def value)  {
    ;
  }
}
'''

  def void test_simple_boolean_expression()  {

    def a = create_instance_of(source_postconditions)
    a.change_property_value('test')

    assertEquals 'test', a.property
  }

  def void test_binary_boolean_expression()  {

    def a = create_instance_of(source_postconditions)
    a.change_property_values('test', 'test2')

    assertEquals 'test', a.property
    assertEquals 'test2', a.property2
  }

  def void test_negated_boolean_expression()  {

    def a = create_instance_of(source_postconditions)
    a.change_property_value_not('test')
  }
}
