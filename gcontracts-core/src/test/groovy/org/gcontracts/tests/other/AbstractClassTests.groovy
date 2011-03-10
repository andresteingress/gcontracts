package org.gcontracts.tests.other

import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * @author ast
 */
class AbstractClassTests extends BaseTestClass {

  def source1 = '''
@Contracted
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
@Contracted
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

  @Test void inherited_class_invariant()  {
    add_class_to_classpath source1

    shouldFail AssertionError, {
       create_instance_of(source2, [null])
    }
  }

  @Test void inherited_precondition()  {
    add_class_to_classpath source1

    def bInstance = create_instance_of(source2, ["test"])

      shouldFail PreconditionViolation.class, {
            bInstance.some_operation null
      }
  }

}
