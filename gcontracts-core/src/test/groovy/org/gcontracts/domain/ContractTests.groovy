package org.gcontracts.domain

import junit.framework.TestCase
import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.MethodNode

class ContractTests extends TestCase {

    void test_create_simple_contract()  {

        def source = '''
        class Tester {

           void some_method()  {}
        }
'''

        AstStringCompiler astStringCompiler = new AstStringCompiler()
        def astNodes = astStringCompiler.compile(source, CompilePhase.SEMANTIC_ANALYSIS, false)
        ClassNode classNode = astNodes[1]
        assertNotNull(classNode)

        Contract contract = new Contract(classNode)

        Precondition precondition = new Precondition(new BooleanExpression(new ConstantExpression(true)))
        contract.addPrecondition(classNode.getMethod("some_method", [] as Parameter[]), precondition)

        assertEquals(1, contract.preconditions().size())
    }
}
