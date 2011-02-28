package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author ast
 */
class ResultVariablePostconditionTests extends BaseTestClass {


    def sourceCode = '''
package tests

import org.gcontracts.annotations.Invariant
import org.gcontracts.annotations.Requires
import org.gcontracts.annotations.Ensures

class ResultVariable {

  @Ensures({ result -> result == other })
  def return_given_argument(def other)  {
    return other
  }
}
'''

  void test_string_return_value()  {

    def instance = create_instance_of(sourceCode)

    assertEquals 'test', instance.return_given_argument('test')
  }

  void test_null_return_value()  {

    def instance = create_instance_of(sourceCode)

    assertEquals null, instance.return_given_argument(null)
  }
}
