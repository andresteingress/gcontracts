package org.gcontracts.domain

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.*
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.syntax.Types
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.*
import org.codehaus.groovy.ast.stmt.BlockStatement;

class ContractTests {

    ClassNode classNode
    MethodNode methodNode

    @Before public void setUp() {
        def source = '''
        class Tester {

           void some_method()  {}
        }
'''

        def astNodes = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, source)
        classNode = astNodes[1]
        assertNotNull(classNode)

        methodNode = classNode.getMethod("some_method", Parameter.EMPTY_ARRAY)
        assertNotNull(methodNode)
    }

    @Test
    void create_simple_contract()  {
        Contract contract = new Contract(classNode)

        Precondition precondition = new Precondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)))
        contract.preconditions().or(classNode.getMethod("some_method", [] as Parameter[]), precondition)

        assertEquals(1, contract.preconditions().size())
    }

    @Test
    void anding_precondition_causes_logical_or()  {

        Contract contract = new Contract(classNode)

        Precondition precondition1 = new Precondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)))
        Precondition precondition2 = new Precondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)))

        contract.preconditions().or(methodNode, precondition1)
        contract.preconditions().or(methodNode, precondition2)

        assertEquals(1, contract.preconditions().size())
        assertTrue(contract.preconditions().get(methodNode).booleanExpression().expression.operation.type == Types.LOGICAL_OR)
    }

    @Test
    void anding_postcondition_causes_logical_and()  {

        Contract contract = new Contract(classNode)

        Postcondition postcondition = new Postcondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)), false)
        Postcondition postcondition1 = new Postcondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)), false)

        contract.postconditions().and(methodNode, postcondition)
        contract.postconditions().and(methodNode, postcondition1)

        assertEquals(1, contract.postconditions().size())
        assertTrue(contract.postconditions().get(methodNode).booleanExpression().expression.operation.type == Types.LOGICAL_AND)
    }

    @Test
    void joining_preconditions()  {

        Contract contract = new Contract(classNode)

        Precondition precondition1 = new Precondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)))
        Precondition precondition2 = new Precondition(new BlockStatement(), new BooleanExpression(new ConstantExpression(true)))

        contract.preconditions().join(methodNode, precondition1)
        contract.preconditions().join(methodNode, precondition2)

        assertEquals(1, contract.preconditions().size())
        assertTrue(contract.preconditions().get(methodNode).booleanExpression().expression.operation.type == Types.LOGICAL_AND)
    }

}
