package org.gcontracts.tests

import org.codehaus.groovy.ast.ClassHelper
import org.gcontracts.generation.CandidateChecks
import org.junit.Test

import static org.junit.Assert.*;

import static org.junit.Assert.*
import org.codehaus.groovy.ast.Parameter;

class A {

}

interface B {

}

enum C {

}

class D {
    private D() {}

    private def method() {}
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

  // refs #22
  @Test void testPrivateConstructors()  {
      def classNode = ClassHelper.make(D.class)
      assertTrue  "private constructors should support preconditions",
              CandidateChecks.isPreconditionCandidate(classNode, classNode.getDeclaredConstructors().first())
      assertTrue  "private methods should support preconditions",
              CandidateChecks.isPreconditionCandidate(classNode, classNode.getMethod("method", [] as Parameter[]))

      assertFalse  "private constructors should by now NOT support class invariants",
              CandidateChecks.isClassInvariantCandidate(classNode, classNode.getDeclaredConstructors().first())
  }
}
