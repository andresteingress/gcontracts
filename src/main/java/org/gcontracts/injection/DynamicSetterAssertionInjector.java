package org.gcontracts.injection;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.ast.AssertStatementCreator;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Assertion injector for injecting class invariants in Groovy generated setter methods.<p/>
 *
 * This AST transformation must be run after execution of {@link org.gcontracts.ast.ContractValidationASTTransformation}.
 *
 * @see org.gcontracts.ast.ContractValidationASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
public class DynamicSetterAssertionInjector extends Injector {

    private final ClassNode classNode;

    public DynamicSetterAssertionInjector(final ClassNode classNode)  {
        this.classNode = classNode;
    }

    public void rewrite()  {

        // if a class invariant is available visit all property nodes else skip this class
        final FieldNode invariantField = getInvariantClosureFieldNode(classNode);
        if (invariantField == null) return;

        new ClassCodeVisitorSupport()  {

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }

            protected Statement createSetterBlock(PropertyNode propertyNode, final FieldNode field, final Parameter parameter) {
                final BlockStatement setterMethodBlock = new BlockStatement();

                // check invariant before assignment
                setterMethodBlock.addStatement(AssertStatementCreator.getInvariantAssertionStatement(classNode, invariantField));

                // do assignment
                BinaryExpression fieldAssignment = new BinaryExpression(new FieldExpression(field), Token.newSymbol(Types.ASSIGN, -1, -1), new VariableExpression(parameter));
                setterMethodBlock.addStatement(new org.codehaus.groovy.ast.stmt.ExpressionStatement(fieldAssignment));


                // check invariant after assignment
                setterMethodBlock.addStatement(AssertStatementCreator.getInvariantAssertionStatement(classNode, invariantField));

                return setterMethodBlock;
            }

            public void visitProperty(PropertyNode node) {
                final String setterName = "set" + MetaClassHelper.capitalize(node.getName());

                final Statement setterBlock = node.getSetterBlock();
                final Parameter parameter = new Parameter(node.getType(), "value");

                if (setterBlock == null && classNode.getMethod(setterName, new Parameter[]{ parameter } ) == null) {
                    final Statement setterBlockStatement = createSetterBlock(node, node.getField(), parameter);
                    node.setSetterBlock(setterBlockStatement);
                }
            }

            @Override
            public void visitClass(ClassNode classNode) {
                super.visitClass(classNode);

                List<ConstructorNode> declaredConstructors = classNode.getDeclaredConstructors();
                if (declaredConstructors == null || declaredConstructors.isEmpty())  {
                    // create default constructor with class invariant check
                    ConstructorNode constructor = new ConstructorNode(Opcodes.ACC_PUBLIC, AssertStatementCreator.getInvariantAssertionStatement(classNode, invariantField));
                    constructor.setSynthetic(true);
                    classNode.addConstructor(constructor);
                }
            }
            
        }.visitClass(classNode);
    }
}
