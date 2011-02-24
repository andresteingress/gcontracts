package org.gcontracts.common.impl;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.ast.visitor.BaseAnnotationProcessingASTTransformation;
import org.gcontracts.util.Validate;

/**
 * Implementation of {@link org.gcontracts.common.spi.AnnotationProcessingASTTransformation} which checks
 * {@link Parameter} instances for null values when {@link org.gcontracts.annotations.common.NotNull} is
 * specified on them.
 *
 * @author andre.steingress@gmail.com
 */
public class NotNullAnnotationProcessor extends BaseAnnotationProcessingASTTransformation {

    @Override
    public void process(ClassNode classNode, MethodNode methodNode, Parameter targetAnnotatedNode) {
        AssertStatement assertStatement = new AssertStatement(new BooleanExpression(new BinaryExpression(new VariableExpression(targetAnnotatedNode), Token.newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1), ConstantExpression.NULL)));
        assertStatement.setMessageExpression(new ConstantExpression("Parameter '" + targetAnnotatedNode.getName() + "' must not be null (marked with @NotNull)!"));
        assertStatement.setSourcePosition(targetAnnotatedNode);

        Validate.isTrue(methodNode.getCode() instanceof BlockStatement);

        BlockStatement blockStatement = (BlockStatement) methodNode.getCode();
        if (hasPreconditionAnnotation(targetAnnotatedNode))  {
            blockStatement.getStatements().add(0, assertStatement);
        }
        if (hasPostconditionAnnotation(targetAnnotatedNode))  {
            blockStatement.getStatements().add(assertStatement);
        }
    }
}
