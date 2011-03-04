package org.gcontracts.tests.post

import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.*;

/**
 * @author ast
 */
class ResultAndOldVariablePostconditionTests extends BaseTestClass {

  def source = '''
package tests

import org.gcontracts.annotations.*

class EnsureVariables {

  private String string1

  public EnsureVariables(final String other)  {
    string1 = other
  }

  @Ensures({ result, old -> result == part1 + "," + part2 && old.string1 == string1 })
  def String concatenateColon(final String part1, final String part2)  {
    return part1 + "," + part2
  }

  @Ensures({ old, result -> result == part1 + "," + part2 && old.string1 == string1 })
  def String concatenateColon2(final String part1, final String part2)  {
    return part1 + "," + part2
  }
}

'''

  @Test void result_than_old_variable()  {
    def var = create_instance_of(source, ['some string'])

    var.concatenateColon("part1", "part2")
  }

  @Test void old_than_result_variable()  {
    def var = create_instance_of(source, ['some string'])

    var.concatenateColon2("part1", "part2")
  }
}
