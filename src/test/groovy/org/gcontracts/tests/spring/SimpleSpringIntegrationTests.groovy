package org.gcontracts.tests.spring

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author andre.steingress@gmail.com
 */
class SimpleSpringIntegrationTests extends BaseTestClass {

  def source1 = '''
package tests

import org.gcontracts.annotations.*
import org.springframework.stereotype.*

@Component
@Invariant({ property != null })
class A {

  def property

  def A(def someValue)  {
    property = someValue
  }
}
'''

  def source2 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }
}
'''

  def source3 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }

  static me = "me"
}
'''

  def void test_defer_invariant_check_to_afterPropertiesSet()  {
    create_instance_of(source1, [null])
  }

  def void test_invariant_check_in_afterPropertiesSet()  {
    def a = create_instance_of(source1, [null])
    shouldFail AssertionError, {
      a.afterPropertiesSet()
    }
  }
}
