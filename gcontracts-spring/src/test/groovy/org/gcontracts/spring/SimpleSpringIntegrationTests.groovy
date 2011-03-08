package org.gcontracts.spring

 /**
 * @author ast
 */
class SimpleSpringIntegrationTests extends BaseTestClass {

  def source1 = '''
@EnableAssertions
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
@EnableAssertions
package tests

import org.springframework.stereotype.*
import org.gcontracts.annotations.*

@Component
@Invariant({ property != null })
class A implements org.springframework.beans.factory.InitializingBean {

  private property

  def A(def someValue)  {
    property = someValue
  }

  void afterPropertiesSet() throws Exception {
   // noop
  }
}
'''

  def source3 = '''
@EnableAssertions
package tests

import org.springframework.stereotype.*
import org.gcontracts.annotations.*

@Component
@Invariant({ property != null })
class A {

  private property

  def A(def someValue)  {
    property = someValue
  }

  @javax.annotation.PostConstruct
  void afterPropertiesSet() throws Exception {
   // noop
  }
}
'''

  void test_defer_invariant_check_to_afterPropertiesSet()  {
    create_instance_of(source1, [null])
  }

  void test_invariant_check_in_afterPropertiesSet()  {
    def a = create_instance_of(source1, [null])
    shouldFail org.gcontracts.ClassInvariantViolation, {
      a.$gcontracts_postConstruct()
    }
  }

  void test_Sinvariant_check_will_be_ignored_for_bean_with_InitializingBean()  {
    def a = create_instance_of(source2, [null])
    shouldFail MissingMethodException, {
      a.$gcontracts_postConstruct()
    }
  }

  void test_Sinvariant_check_will_be_ignored_for_bean_with_PostConstruct()  {
    def a = create_instance_of(source3, [null])
    shouldFail MissingMethodException, {
      a.$gcontracts_postConstruct()
    }
  }
}
