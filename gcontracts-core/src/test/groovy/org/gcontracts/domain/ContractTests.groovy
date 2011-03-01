package org.gcontracts.domain

import junit.framework.TestCase
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.syntax.Types

class ContractTests extends TestCase {

    ClassNode classNode
    MethodNode methodNode

    @Override protected void setUp() {
        def source = '''
        class Tester {

           void some_method()  {}
        }
'''

        AstStringCompiler astStringCompiler = new AstStringCompiler()
        def astNodes = astStringCompiler.compile(source, CompilePhase.SEMANTIC_ANALYSIS, false)
        classNode = astNodes[1]
        assertNotNull(classNode)

        methodNode = classNode.getMethod("some_method", Parameter.EMPTY_ARRAY)
        assertNotNull(methodNode)
    }

    void test_create_simple_contract()  {
        Contract contract = new Contract(classNode)

        Precondition precondition = new Precondition(new BooleanExpression(new ConstantExpression(true)))
        contract.addPrecondition(classNode.getMethod("some_method", [] as Parameter[]), precondition)

        assertEquals(1, contract.preconditions().size())
    }

    void test_anding_precondition_causes_logical_or()  {

        Contract contract = new Contract(classNode)

        Precondition precondition1 = new Precondition(new BooleanExpression(new ConstantExpression(true)))
        Precondition precondition2 = new Precondition(new BooleanExpression(new ConstantExpression(true)))

        contract.addPrecondition(methodNode, precondition1)
        contract.addPrecondition(methodNode, precondition2)

        assertEquals(1, contract.preconditions().size())
        assertTrue(contract.preconditions().get(methodNode).booleanExpression().expression.operation.type == Types.LOGICAL_OR)
    }

    void test_anding_postcondition_causes_logical_and()  {

        Contract contract = new Contract(classNode)

        Postcondition postcondition = new Postcondition(new BooleanExpression(new ConstantExpression(true)))
        Postcondition postcondition1 = new Postcondition(new BooleanExpression(new ConstantExpression(true)))

        contract.addPostcondition(methodNode, postcondition)
        contract.addPostcondition(methodNode, postcondition1)

        assertEquals(1, contract.postconditions().size())
        assertTrue(contract.postconditions().get(methodNode).booleanExpression().expression.operation.type == Types.LOGICAL_AND)
    }
}
