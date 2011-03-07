package org.gcontracts.tests.interfaces

import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * @author ast
 */
class AbstractClassInheritanceTests extends BaseTestClass {

  def source_stackable = '''
@AssertionsEnabled
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
@AssertionsEnabled
package tests

import org.gcontracts.annotations.*

@Invariant({ list != null && anotherName != null })
class Stack extends Stackable  {

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
  void push(def item)  {
    list.add item
  }

  void multi_push(def item1, def item2)  {
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

  void modifyClassInvariant()  {
    anotherName = null
  }
}
'''

  @Test void creation()  {
    add_class_to_classpath(source_stackable)
    create_instance_of(source_stack)
  }

  @Test void push_precondition()  {
    add_class_to_classpath(source_stackable)

    def stack = create_instance_of(source_stack)

    shouldFail PreconditionViolation.class, {
        stack.push null
    }
  }
}
