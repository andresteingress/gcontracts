package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.*;

/**
 * @author ast
 */
class ResultVariablePostconditionTests extends BaseTestClass {


    def sourceCode = '''
@EnableAssertions
package tests

import org.gcontracts.annotations.*

class ResultVariable {

  @Ensures({ result -> result == other })
  def return_given_argument(def other)  {
    return other
  }
}
'''

  @Test void string_return_value()  {

    def instance = create_instance_of(sourceCode)

    assertEquals 'test', instance.return_given_argument('test')
  }

  @Test void null_return_value()  {

    def instance = create_instance_of(sourceCode)

    assertEquals null, instance.return_given_argument(null)
  }
}
