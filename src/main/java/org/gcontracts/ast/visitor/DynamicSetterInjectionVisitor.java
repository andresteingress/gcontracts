package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.generation.BaseGenerator;
import org.gcontracts.generation.AssertStatementCreationUtility;
import org.gcontracts.generation.CandidateChecks;
import org.gcontracts.util.ClosureToSourceConverter;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class DynamicSetterInjectionVisitor extends BaseVisitor {

    private ClosureExpression closureExpression;
    private String closureSourceCode;

    public DynamicSetterInjectionVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        super(sourceUnit, source);
    }

    protected Statement createSetterBlock(final ClassNode classNode, final FieldNode field, final Parameter parameter) {
        final BlockStatement setterMethodBlock = new BlockStatement();

        // check invariant before assignment

        setterMethodBlock.addStatement(AssertStatementCreationUtility.getInvariantAssertionStatement(classNode, closureExpression, closureSourceCode));

        // do assignment
        BinaryExpression fieldAssignment = new BinaryExpression(new FieldExpression(field), Token.newSymbol(Types.ASSIGN, -1, -1), new VariableExpression(parameter));
        setterMethodBlock.addStatement(new org.codehaus.groovy.ast.stmt.ExpressionStatement(fieldAssignment));


        // check invariant after assignment
        setterMethodBlock.addStatement(AssertStatementCreationUtility.getInvariantAssertionStatement(classNode, closureExpression, closureSourceCode));

        return setterMethodBlock;
    }

    public void visitProperty(PropertyNode node) {
        final ClassNode classNode = node.getDeclaringClass();
        final String setterName = "set" + MetaClassHelper.capitalize(node.getName());

        final Statement setterBlock = node.getSetterBlock();
        final Parameter parameter = new Parameter(node.getType(), "value");

        if (CandidateChecks.isClassInvariantCandidate(node) && (setterBlock == null && classNode.getMethod(setterName, new Parameter[]{ parameter } ) == null)) {
            final Statement setterBlockStatement = createSetterBlock(classNode, node.getField(), parameter);
            node.setSetterBlock(setterBlockStatement);
        }
    }

    @Override
    public void visitClass(ClassNode classNode) {
        // if a class invariant is available visit all property nodes else skip this class
        final FieldNode invariantField = BaseGenerator.getInvariantClosureFieldNode(classNode);
        if (invariantField == null) return;

        closureExpression = (ClosureExpression) invariantField.getInitialValueExpression();
        closureSourceCode = ClosureToSourceConverter.convert(closureExpression, source);

        List<ConstructorNode> declaredConstructors = classNode.getDeclaredConstructors();
        if (declaredConstructors == null || declaredConstructors.isEmpty())  {
            // create default constructor with class invariant check
            ConstructorNode constructor = new ConstructorNode(Opcodes.ACC_PUBLIC, AssertStatementCreationUtility.getInvariantAssertionStatement(classNode, closureExpression, closureSourceCode));
            constructor.setSynthetic(true);
            classNode.addConstructor(constructor);
        }

        super.visitClass(classNode);
    }
}
