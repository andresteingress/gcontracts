package org.gcontracts.tests.interfaces

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author ast
 */
class SimpleInterfaceInheritanceTests extends BaseTestClass {

  def source_stackable = '''
package tests

import org.gcontracts.annotations.*

abstract class Stackable {

  @Requires({ item != null })
  abstract void push(def item)
}

'''

  def source_stack = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ list != null && anotherName != null })
class Stack extends Stackable {

  protected def list
  def anotherName = ""
  def protected name = ""

  public Stack()  {
    this.list = []
  }

  public Stack(def list)  {
    this.list = list
  }

  @Requires({ item > 2 })
  @Ensures({ list[-1] == item })
  void push(def item)  {
    list.add item
  }
}
'''

    def source_implicit_interface = '''
import org.gcontracts.annotations.*

interface A {
   @Ensures({ old != null && result != null })
   def some_method()
}

class B implements A {

   def some_method() { return null }

}

class C extends B {
   def some_method() { return null }
}
'''

    def source_implicit_interface2 = '''
import org.gcontracts.annotations.*

class C extends B {
   def some_method() { return null }
}
'''

  void test_creation()  {
    add_class_to_classpath(source_stackable)
    create_instance_of(source_stack)
  }

  void test_push_precondition()  {
    add_class_to_classpath(source_stackable)

    def stack = create_instance_of(source_stack)

    shouldFail AssertionError, {
        stack.push null
    }

    stack.push 1
    stack.push 2
  }

  void test_postcondition_in_indirect_parent_interface()  {
    add_class_to_classpath(source_implicit_interface)
    def c = create_instance_of(source_implicit_interface2)

    shouldFail AssertionError, {
        c.some_method()
    }
  }

}
