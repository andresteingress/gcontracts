package org.gcontracts.tests.interfaces

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author ast
 */
class AbstractClassTests extends BaseTestClass {

  def source_stackable = '''
package tests

import org.gcontracts.annotations.*

abstract class Stackable {

  @Requires({ item != null })
  abstract void push(def item)

  @Requires({ item1 != null && item2 != null })
  abstract void multi_push(def item1, def item2)
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

  @Ensures({ list[-1] == item })
  def void push(def item)  {
    list.add item
  }

  def void multi_push(def item1, def item2)  {
    push item1
    push item2
  }

//  @Requires({ list.size() > 0 })
//  @Ensures({ result != null })
//  def Object pop()  {
//    list[-1]
//  }

  @Ensures({ result -> result == list.size() })
  def int size()  {
    return list.size()
  }

  @Ensures({ result -> comp1 != null && comp2 != null && result > 0 })
  def int size(def comp1, comp2)  {
      return comp1 + comp2
  }

  @Ensures({ result -> result == 'tostring'})
  @Override
  def String toString()  {
    return 'tostring'
  }

  def void modifyClassInvariant()  {
    anotherName = null
  }
}
'''

  void test_creation()  {
    add_class_to_classpath(source_stackable)
    create_instance_of(source_stack)
  }

  void test_push_precondition()  {
    add_class_to_classpath(source_stackable)
    def stack = create_instance_of(source_stack)

    stack.push null
  }
}
