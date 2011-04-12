package org.gcontracts.tests.inv

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

 /**
 * POGO class invariant tests.
 *
 * @author ast
 */
class POGOClassInvariantTests extends BaseTestClass {

  def dynamic_constructor_class_code = '''
@Contracted
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class DynamicConstructor {

  def property
}
'''

  def dynamic_setter_class_code = '''
@Contracted
package tests

import org.gcontracts.annotations.*

@Invariant({ string1 != null && string2 != null && string3 != null })
class DynamicSetter {

  String string1 = ''
  def String string2 = ''
  final String string3 = ''
}

'''

  @Test void dynamic_constructor_class_invariant()  {
    shouldFail AssertionError, {
      create_instance_of dynamic_constructor_class_code;
    }
  }

  @Test void dynamic_setter_methods()  {
    def instance = create_instance_of(dynamic_setter_class_code)

    shouldFail AssertionError, {
      instance.string1 = null
    }

    shouldFail AssertionError, {
      instance.string2 = null
    }

    shouldFail AssertionError, {
      instance.string3 = null
    }
  }
}