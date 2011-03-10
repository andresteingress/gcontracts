package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.*;

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
}
