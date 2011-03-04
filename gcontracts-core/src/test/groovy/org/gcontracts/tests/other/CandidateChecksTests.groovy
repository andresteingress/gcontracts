package org.gcontracts.tests

import org.codehaus.groovy.ast.ClassHelper
import org.gcontracts.generation.CandidateChecks
import org.junit.Test

import static org.junit.Assert.*;

import static org.junit.Assert.*;

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
 * @author ast
 */
class CandidateChecksTests {

  @Test void testContractsCandidateChecks()  {
    assertFalse CandidateChecks.isContractsCandidate(ClassHelper.make(B.class))
    assertFalse CandidateChecks.isContractsCandidate(ClassHelper.make(C.class))
    assertTrue  CandidateChecks.isContractsCandidate(ClassHelper.make(A.class))
  }
}
