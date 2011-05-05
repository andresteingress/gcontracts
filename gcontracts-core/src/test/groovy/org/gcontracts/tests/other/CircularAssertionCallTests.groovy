package org.gcontracts.tests.other

import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

 /**
 * @author ast
 */
class CircularAssertionCallTests extends BaseTestClass {

    @Test void detectCircularAssertionCalls()  {

        def source = '''
@Contracted
package tests

import org.gcontracts.annotations.*

class A {

  @Requires({ isConditionB() })
  def isConditionA() { return false }

  @Requires({ isConditionA() })
  def isConditionB() { return true }
}
'''

        def a = create_instance_of(source)

        shouldFail PreconditionViolation, {
            a.isConditionB()
        }
    }

    @Test void detect_diamon_assertion_calls()  {

        def source = '''
@Contracted
package tests

import org.gcontracts.annotations.*

class A {

  @Requires({ isConditionC() })
  def the_method_to_call() {}

  @Requires({ isConditionA() && isConditionB() })
  def isConditionC() {}

  @Requires({ isConditionC() })
  def isConditionA() {}

  @Requires({ isConditionC() })
  def isConditionB() {}
}
'''

        def a = create_instance_of(source)
        shouldFail PreconditionViolation, {
            a.isConditionB()
        }
    }
}
