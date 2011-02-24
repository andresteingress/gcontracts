package org.gcontracts.tests

import org.codehaus.groovy.ast.ClassHelper
import org.gcontracts.generation.CandidateChecks

class A {

}

interface B {

}

enum C {

}

/**
 * all test cases for {@link CandidateChecks}.
 *
 * @see CandidateChecks
 *
 * @author andre.steingress@gmail.com
 */
class CandidateChecksTests extends GroovyTestCase {

  def void testContractsCandidateChecks()  {
    assertFalse CandidateChecks.isContractsCandidate(ClassHelper.make(B.class))
    assertFalse CandidateChecks.isContractsCandidate(ClassHelper.make(C.class))
    assertTrue  CandidateChecks.isContractsCandidate(ClassHelper.make(A.class))
  }
}
