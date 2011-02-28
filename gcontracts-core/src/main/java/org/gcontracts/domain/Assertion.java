package org.gcontracts.domain;

import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.gcontracts.util.Validate;

/**
 * Created by IntelliJ IDEA.
 * User: andre
 * Date: 26.02.11
 * Time: 21:14
 * To change this template use File | Settings | File Templates.
 */
public abstract class Assertion<T extends Assertion> {

    private BooleanExpression booleanExpression;

    public Assertion()  {
        this.booleanExpression = new BooleanExpression(ConstantExpression.TRUE);
    }

    public Assertion(final BooleanExpression booleanExpression)  {
        Validate.notNull(booleanExpression);
        this.booleanExpression = booleanExpression;
    }

    public BooleanExpression booleanExpression() { return booleanExpression; }

    public void renew(BooleanExpression booleanExpression)  {
        Validate.notNull(booleanExpression);
        this.booleanExpression = booleanExpression;
    }

    public void and(T other) {
        Validate.notNull(other);

        BooleanExpression newBooleanExpression =
                new BooleanExpression(
                        new BinaryExpression(
                                booleanExpression(),
                                Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                                other.booleanExpression()
                        )
                );

        renew(newBooleanExpression);
    }

    public void or(T other) {
        Validate.notNull(other);

        BooleanExpression newBooleanExpression =
                new BooleanExpression(
                        new BinaryExpression(
                                booleanExpression(),
                                Token.newSymbol(Types.LOGICAL_OR, -1, -1),
                                other.booleanExpression()
                        )
                );

        renew(newBooleanExpression);
    }
}
