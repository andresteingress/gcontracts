package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author andre.steingress@gmail.com
 */
class InheritanceTests extends BaseTestClass {
  
  def source_parent = '''
package tests

import org.gcontracts.annotations.*

class Parent {

  def Integer field1

  @Ensures({ field1 == param1 })
  def void some_operation(Integer param1)  {
    field1 = param1
  }

  @Ensures({ false })
  def void some_operation2(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 != field1 })
  def void some_operation3(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 != field1 })
  def void some_operation4(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 == field1 })
  def void some_operation5(Integer param1) {
    field1 = param1
  }

  @Ensures({ false })
  def Integer some_operation6() {
    return 0
  }

  @Ensures({ result -> result >= 0 })
  def Integer some_operation7() {
    return 0
  }

  @Ensures({ result, old -> result >= 0 && old.field1 == field1 })
  def Integer some_operation8() {
    return 0
  }

  @Ensures({ false })
  def Integer some_operation9() {
    return 0
  }
}
'''
  
  def source_descendant = '''
package tests

import org.gcontracts.annotations.*

class Descendant extends Parent {

  @Override
  @Ensures({ false })
  def void some_operation(Integer param1) {
    field1 = param1  
  }

  @Override
  @Ensures({ field1 == param1 })
  def void some_operation2(Integer param1) {
    field1 = param1
  }

  @Ensures({ true })                                             
  def void some_operation3(Integer param1) {
    field1 = param1
  }

  @Ensures({ false })
  def void some_operation4(Integer param1) {
    field1 = param1
  }

  @Ensures({ old -> old.field1 != field1 })
  def void some_operation5(Integer param1) {
    field1 = param1
  }

  @Ensures({ result -> result >= 0 })
  def Integer some_operation6() {
    return 0
  }
  
  @Ensures({ true })
  def Integer some_operation7() {
    return 0
  }

  @Ensures({ true })
  def Integer some_operation8() {
    return 0
  }

  def Integer some_operation9() {
    return 0
  }
}
'''
  
  def void test_inherited_postcondition()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation 0
    }

  }

  def void test_inherited_postcondition_with_fail_in_parent()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation2 0
    }

  }

  def void test_inherited_postcondition_with_old_variable()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    child.some_operation3 0
  }

  def void test_inherited_postcondition_fail_with_old_variable()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation4 0
    }
  }

  def void test_inherited_postcondition_fail()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation5 0
    }
  }

  def void test_inherited_postcondition_fail_with_result_variable()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation6()
    }
  }

  def void test_inherited_postcondition_with_result_variable()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    child.some_operation7()
  }

  def void test_inherited_postcondition_with_result_and_old_variables()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    child.some_operation8()
  }

  def void test_inherited_postcondition_fail_with_result_variable2()  {

    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation9()
    }
  }
  
}
