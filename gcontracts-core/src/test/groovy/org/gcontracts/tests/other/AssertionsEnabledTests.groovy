package org.gcontracts.tests.other

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

 /**
 * @author ast
 */
class AssertionsEnabledTests extends BaseTestClass {

  def source = '''
@AssertionsEnabled
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {

  def property
}
'''

    def source2 = '''
import org.gcontracts.annotations.*

@AssertionsEnabled
@Invariant({ property != null })
class A {

  def property
}
'''

  @Test void AssertionsEnabled_on_package_level()  {
    add_class_to_classpath source

    shouldFail AssertionError, {
       create_instance_of(source)
    }
  }

  @Test void AssertionsEnabled_on_class_level()  {
    add_class_to_classpath source2

    shouldFail AssertionError, {
       create_instance_of(source2)
    }
  }
}
