package org.gcontracts.tests.pre

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * @author ast
 */
class InheritanceTests extends BaseTestClass {

  def source_parent = '''
@EnableAssertions
package tests

import org.gcontracts.annotations.*

class Parent {

  @Requires({ true })
  def Parent()  {

  }

  @Requires({ param1 > 0 && param2 > 0  })
  def some_operation1(Integer param1, Integer param2)  {

  }

  boolean boolean_operation() {
    println "blue"
    return true
  }

  @Requires({ param1 > 0 && param2 > 1 })
  def some_operation3(Integer param1, Integer param2)  {

  }

  @Requires({ param1 > 0 && param2 > 0 })
  def some_operation4(Integer param1, Integer param2)  {
    println param1
    println param2
  }
}
'''

  def source_descendant = '''
@EnableAssertions
package tests

import org.gcontracts.annotations.*

class Descendant extends Parent {

  @Requires({ true })
  @Ensures({ true })
  def Descendant()  {
    super()
  }

  @Override
  @Requires({ param1 > 1 && param2 > 1  })
  def some_operation1(Integer param1, Integer param2)  {

  }

  @Requires({ boolean_operation() })
  def some_operation2()  {

  }

  @Requires({ x > 0 && y > 0 })
  def some_operation3(Integer x, Integer y)  {

  }

}
'''

  @Test void redefined_precondition() throws Exception {
    // create_instance_of(source_parent)
      add_class_to_classpath(source_parent)
    def child = create_instance_of(source_descendant)

    child.some_operation1(1, 1)
  }

  @Test void redefined_precondition2() throws Exception {
    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation1(0, 0)
    }
  }

  @Test void method_call_of_super_class_in_precondition() throws Exception {
    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    println child.boolean_operation()

    child.some_operation2()
  }

  @Test void refined_precondition_with_other_param_names() throws Exception {
    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation3(0, 0)
    }
  }

  @Test void refined_precondition_with_other_param_names1() throws Exception {
    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    shouldFail AssertionError, {
      child.some_operation3(0, 1)
    }
  }

  @Test void refined_precondition_with_other_param_names2() throws Exception {
    create_instance_of(source_parent)
    def child = create_instance_of(source_descendant)

    child.some_operation3(1, 2)
  }


}
