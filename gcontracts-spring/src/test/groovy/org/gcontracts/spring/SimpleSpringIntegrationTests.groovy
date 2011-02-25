package org.gcontracts.spring

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

  def void test_defer_invariant_check_to_afterPropertiesSet()  {
    create_instance_of(source1, [null])
  }

  def void test_invariant_check_in_afterPropertiesSet()  {
    def a = create_instance_of(source1, [null])
    shouldFail AssertionError, {
      a.$gcontracts_postConstruct()
    }
  }

  def void test_Sinvariant_check_will_be_ignored_for_bean_with_InitializingBean()  {
    def a = create_instance_of(source2, [null])
    shouldFail MissingMethodException, {
      a.$gcontracts_postConstruct()
    }
  }

  def void test_Sinvariant_check_will_be_ignored_for_bean_with_PostConstruct()  {
    def a = create_instance_of(source3, [null])
    shouldFail MissingMethodException, {
      a.$gcontracts_postConstruct()
    }
  }
}
