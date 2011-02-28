package org.gcontracts.domain;

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;

/**
 * Created by IntelliJ IDEA.
 * User: andre
 * Date: 26.02.11
 * Time: 21:09
 * To change this template use File | Settings | File Templates.
 */
public class ClassInvariant extends Assertion<ClassInvariant> {

    public static final ClassInvariant DEFAULT = new ClassInvariant(new BooleanExpression(new ConstantExpression(true)));

    public ClassInvariant() {}

    public ClassInvariant(BooleanExpression booleanExpression) {
        super(booleanExpression);
    }
}
