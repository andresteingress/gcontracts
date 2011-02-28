package org.gcontracts.domain;

import org.codehaus.groovy.ast.expr.BooleanExpression;

/**
 * Created by IntelliJ IDEA.
 * User: andre
 * Date: 26.02.11
 * Time: 21:09
 * To change this template use File | Settings | File Templates.
 */
public class Postcondition extends Assertion<Postcondition> {

    public Postcondition() {}

    public Postcondition(BooleanExpression booleanExpression) {
        super(booleanExpression);
    }
}
