package org.gcontracts.tests.other

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author ast
 */
class AbstractClassTests extends BaseTestClass {

  def source1 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
abstract class A {

  def property

  def A(def someValue)  {
    property = someValue
  }

  @Requires({ param != null })
  def some_operation(def param)  {
    // noop
  }
}
'''

  def source2 = '''
package tests

import org.gcontracts.annotations.*

class B extends A  {

  def B(def someValue)  {
    super(someValue)
  }

  def some_operation(def param)  {
    // noop
  }
}
'''

  def void test_inherited_class_invariant()  {
    add_class_to_classpath source1

    shouldFail AssertionError, {
       create_instance_of(source2, [null])
    }
  }

  def void test_inherited_precondition()  {
    add_class_to_classpath source1

    def bInstance = create_instance_of(source2, ["test"])
    bInstance.some_operation null
  }

}
