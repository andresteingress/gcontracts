package org.gcontracts.domain

import junit.framework.TestCase
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase

class ContractTests extends TestCase {

    interface Stackable {

    }

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

    void test_source_position_after_precondition_and()  {

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

        def booleanExpression = new BooleanExpression(new ConstantExpression(true))
        booleanExpression.setLineNumber(1)
        booleanExpression.setLastLineNumber(1)
        booleanExpression.setColumnNumber(1)
        booleanExpression.setLastColumnNumber(1)

        Precondition precondition = new Precondition(booleanExpression)
        MethodNode methodNode = classNode.getMethod("some_method", [] as Parameter[])

        contract.addPrecondition(methodNode, precondition)
        assertEquals(1, contract.preconditions().size())

        contract.addPrecondition(methodNode, new Precondition(new BooleanExpression(new ConstantExpression(true))))
        assertEquals(1, contract.preconditions().size())

        assertTrue(contract.preconditions().get(methodNode).booleanExpression().getLineNumber() == -1)
        assertTrue(contract.preconditions().get(methodNode).booleanExpression().getLastLineNumber() == -1)
        assertTrue(contract.preconditions().get(methodNode).booleanExpression().getColumnNumber() == -1)
        assertTrue(contract.preconditions().get(methodNode).booleanExpression().getLastColumnNumber() == -1)
    }

    void test_add_interface_contract()  {

        def source = '''
        import org.gcontracts.annotations.*

        class Tester implements MyContract {

           void some_method(def param)  {}
        }

        interface MyContract  {

           @Requires({ param != null })
           void some_method(def param)
        }
'''

        AstStringCompiler astStringCompiler = new AstStringCompiler()
        def astNodes = astStringCompiler.compile(source, CompilePhase.SEMANTIC_ANALYSIS, false)
        ClassNode classNode = astNodes[1]
        assertNotNull(classNode)

        ClassNode myContractInterface = astNodes[2]

        def contract = new Contract(classNode)
        def interfaceContract = new Contract(myContractInterface)
        interfaceContract.addPrecondition(myContractInterface.getMethod("some_method", [new Parameter(ClassHelper.OBJECT_TYPE, "param")] as Parameter[]),
            new Precondition(new BooleanExpression(new ConstantExpression(true))))

        contract.addInterfaceContract(interfaceContract)

        assertEquals(1, contract.preconditions().size())
    }
}
