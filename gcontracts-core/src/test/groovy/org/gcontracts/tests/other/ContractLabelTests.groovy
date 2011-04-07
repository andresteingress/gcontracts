package org.gcontracts.tests.other

import org.gcontracts.ClassInvariantViolation
import org.gcontracts.PostconditionViolation
import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

/**
 * @author ast
 */
class ContractLabelTests extends BaseTestClass {

  @Test void class_invariant()  {
    def source1 = '''
package tests

import org.gcontracts.annotations.*

@Invariant({
    not_null_property: property != null
})
class A {

  def property
}
'''

    shouldFail ClassInvariantViolation, {
       create_instance_of(source1)
    }
  }

  @Test void precondition()  {
    def source1 = '''
package tests

import org.gcontracts.annotations.*

class A {
@Requires({
    not_null_param: param != null
})
def some_operation(def param) {
 param
}
}
'''

    shouldFail PreconditionViolation, {
       def a = create_instance_of(source1)
       a.some_operation null
    }
  }

  @Test void postcondition()  {
    def source1 = '''
package tests

import org.gcontracts.annotations.*

class A {
@Ensures({
    result_is_a_result: result == param
})
def some_operation(def param) {
 null
}
}
'''

    shouldFail PostconditionViolation, {
       def a = create_instance_of(source1)
       a.some_operation "test"
    }
  }
}
