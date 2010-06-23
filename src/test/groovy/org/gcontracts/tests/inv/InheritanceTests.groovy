package org.gcontracts.tests.inv

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author andre.steingress@gmail.com
 */
class InheritanceTests extends BaseTestClass {

  def source1 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property != null })
class A {
  def property

  def A(def value) { property = value }
}
'''

  def source2 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property2 != null })
class B extends A {
  def property2

  def B(def value, def value2) { super(value); property2 = value2 }

  def set_values(def value, def value2)  {
    property = value
    property2 = value2
  }
}

'''

  def source3 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property2 != null })
class C extends B {
  def property3

  def C(def value, def value2, def value3) { super(value, value2); property3 = value3 }
}

'''

  def source11 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ property?.size() > 0 })
class A {
   private String property

   def A(String value) { property = value }
}

'''

  def source12 = '''
package tests

import org.gcontracts.annotations.*

class B extends A {
   def B(String value) { super(value) }
}

'''

  def source21 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ prop1 != null && prop2 != null })
class PrivateConstructor {

  def prop1
  def prop2

  def PrivateConstructor(def arg1, def arg2)  {
    prop1 = arg1
    prop2 = arg2
  }

  private PrivateConstructor()  {}
}
'''

  def source31 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ isAvailable() == true })
abstract class A {

  abstract boolean isAvailable()
}

'''

  def source32 = '''
package tests

import org.gcontracts.annotations.*

class B extends A {
  boolean isAvailable() { return true }
}

'''

  def source41 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({ isAvailable() == true })
class A {
    def boolean isAvailable() { return true }
}
'''


  def void test_two_way_inheritance_path()  {
    create_instance_of(source1, ['test'])
    create_instance_of(source2, ['test', 'test2'])

    shouldFail AssertionError, {
      create_instance_of(source2, [null, 'test2'])
    }
  }

  def void test_three_way_inheritance_path()  {
    create_instance_of(source1, ['test'])
    create_instance_of(source2, ['test', 'test2'])
    create_instance_of(source3, ['test', 'test2', 'test3'])

    shouldFail AssertionError, {
      create_instance_of(source3, [null, 'test2', 'test3'])
    }

    shouldFail AssertionError, {
      create_instance_of(source3, [null, null, 'test3'])
    }

    shouldFail AssertionError, {
      create_instance_of(source3, ['test', null, 'test3'])
    }
  }

  def void test_with_private_instance_variable_in_super_class()  {
    create_instance_of(source11, ['test'])
    create_instance_of(source12, ['test'])

    shouldFail AssertionError, {
      create_instance_of(source12, [''])
    }
  }

  def void test_invariant_check_on_method_call()  {
    create_instance_of(source1, ['test'])
    def b = create_instance_of(source2, ['test', 'test2'])

    shouldFail AssertionError, {
      b.set_values(null, null)
    }

    shouldFail AssertionError, {
      b.set_values(null, '')
    }

    shouldFail AssertionError, {
      b.set_values('', null)
    }
  }

  def void test_private_constructor_creation()  {
    create_instance_of(source21)
  }

  def void test_public_constructor_creation()  {
    shouldFail AssertionError, {
      create_instance_of(source21, [ 'test1', null ])
    }
  }

  def void test_with_abstract_class_invariant()  {

    add_class_to_classpath(source31)
    def b = create_instance_of(source32)
  }

  def void test_recursive_class_invariant_call()  {

    def a = create_instance_of(source41)
  }
}
