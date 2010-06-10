package org.gcontracts.generation;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.annotations.Invariant;
import org.gcontracts.util.AnnotationUtils;
import org.objectweb.asm.Opcodes;

/**
 * <p>
 * Code generator for class invariants.
 * </p>
 *
 * @author andre.steingress@gmail.com
 */
public class ClassInvariantGenerator extends BaseGenerator {

    public ClassInvariantGenerator(final ReaderSource source) {
        super(source);
    }


    /**
     * Reads the {@link org.gcontracts.annotations.Invariant} closure expression and adds a class-invariant to
     * all declard contructors of that <tt>type</tt>.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param classInvariant the {@link org.codehaus.groovy.ast.expr.ClosureExpression} containing the assertion expression
     */
    public void generateInvariantAssertionStatement(final ClassNode type, final ClosureExpression classInvariant)  {

        // adding super-calls to invariants of parent classes
        addCallsToSuperClassInvariants(type, classInvariant);

        // add a local protected field with the invariant closure - this is needed for invariant checks in inheritance lines
        type.addField(getInvariantClosureFieldName(type), Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.CLOSURE_TYPE, classInvariant);

        final BlockStatement assertionBlock = new BlockStatement();

        assertionBlock.addStatement(AssertStatementCreationUtility.getInvariantAssertionStatement(type, classInvariant));

        for (ConstructorNode constructor : type.getDeclaredConstructors())  {
            if (CandidateChecks.isClassInvariantCandidate(constructor))  {
                ((BlockStatement) constructor.getCode()).addStatement(assertionBlock);
            }
        }
    }

    /**
     * Generates a default class invariant always being <tt>true</tt>. This is needed in order to allow class invariant
     * inheritance over an inheritance path where only some classes are annotated with <code>@Invariant</code>.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode} to be extended by a default class invariant
     *
     * @return a generated {@link org.codehaus.groovy.ast.expr.ClosureExpression} with a default class invariant
     */
    public ClosureExpression generateDefaultInvariantAssertionStatement(final ClassNode type) {
        BlockStatement closureBlockStatement = new BlockStatement();
        closureBlockStatement.addStatement(new ExpressionStatement(new BooleanExpression(ConstantExpression.TRUE)));

        ClosureExpression closureExpression = new ClosureExpression(null, closureBlockStatement);
        closureExpression.setVariableScope(new VariableScope());
        closureExpression.setSynthetic(true);

        generateInvariantAssertionStatement(type, closureExpression);

        return closureExpression;
    }

    /**
     * Modifies the given <tt>closure</tt> which contains that current class-invariant and adds a super-call the
     * the class-invariant of the next parent class which has the Invarian annotation.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param closure the current class-invariant as {@link org.codehaus.groovy.ast.expr.ClosureExpression}
     */
    public void addCallsToSuperClassInvariants(final ClassNode type, final ClosureExpression closure)  {

        final ClassNode nextClassWithInvariant = AnnotationUtils.getClassNodeInHierarchyWithAnnotation(type.getSuperClass(), Invariant.class);
        if (nextClassWithInvariant == null) return;

        final String fieldName = getInvariantClosureFieldName(nextClassWithInvariant);
        FieldNode nextClassInvariantField = getInvariantClosureFieldNode(nextClassWithInvariant);
        if (nextClassInvariantField == null)  {
            nextClassInvariantField = new FieldNode(fieldName, Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC, ClassHelper.CLOSURE_TYPE, nextClassWithInvariant, null);
        }

        final BlockStatement blockStatement = (BlockStatement) closure.getCode();
        final ExpressionStatement expressionStatement = (ExpressionStatement) blockStatement.getStatements().get(0);

        final Expression expression = expressionStatement.getExpression();

        expressionStatement.setExpression(
                 new BinaryExpression(
                         new BooleanExpression(expression),
                         Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                         new BooleanExpression(new MethodCallExpression(new PropertyExpression(VariableExpression.THIS_EXPRESSION, fieldName), "call", ArgumentListExpression.EMPTY_ARGUMENTS))));
    }

    /**
     * Adds the current class-invariant to the given <tt>method</tt>.
     *
     * @param method the current {@link org.codehaus.groovy.ast.MethodNode}
     * @param classInvariant the {@link org.codehaus.groovy.ast.expr.ClosureExpression} containing the assertion expression
     */
    public void generateInvariantAssertionStatement(final ClassNode type, final MethodNode method, final ClosureExpression classInvariant)  {

        final BlockStatement assertionBlock = new BlockStatement();
        assertionBlock.addStatement(AssertStatementCreationUtility.getInvariantAssertionStatement(method.getDeclaringClass(), classInvariant));

        final Statement statement = method.getCode();
        if (statement instanceof BlockStatement && method.getReturnType() != ClassHelper.VOID_TYPE)  {
            final BlockStatement blockStatement = (BlockStatement) statement;
            final int numberOfStatements = blockStatement.getStatements().size();

            blockStatement.getStatements().add(numberOfStatements > 0 ? numberOfStatements - 1 : 0, assertionBlock);
        } else  {
            assertionBlock.getStatements().add(0, statement);
            method.setCode(assertionBlock);
        }
    }

}
