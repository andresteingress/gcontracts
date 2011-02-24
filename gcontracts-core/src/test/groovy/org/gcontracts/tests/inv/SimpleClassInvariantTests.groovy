package org.gcontracts.tests.inv

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author andre.steingress@gmail.com
 */
class SimpleClassInvariantTests extends BaseTestClass {

  def source1 = '''
package tests

import org.gcontracts.annotations.*

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

  def void test_class_invariant()  {
    create_instance_of(source1, ['test'])

    shouldFail AssertionError, {
       create_instance_of(source1, [null])
    }
  }

  def void test_class_invariant_with_private_instance_variable()  {
    create_instance_of(source2, ['test'])

    shouldFail AssertionError, {
       create_instance_of(source2, [null])
    }
  }

  def void test_class_with_constant()  {
    create_instance_of(source3, ['test'])
  }

}
