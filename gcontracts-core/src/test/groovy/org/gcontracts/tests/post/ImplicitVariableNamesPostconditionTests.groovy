package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass

/**
 * @author ast
 */
class ImplicitVariableNamesPostconditionTests extends BaseTestClass {

  def source = '''
package tests

import org.gcontracts.annotations.*

class EnsureVariables {

  @Ensures({ result -> result == part1 + "," + part2 })
  def String concatenateColon(final String part1, final String part2)  {
    def result = part1 + "," + part2
    return result
  }
}

'''

  void testing_result_with_result_variable_in_method_block()  {
    //def var = create_instance_of(source, [])

    //var.concatenateColon("part1", "part2")
  }
}
