package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * <p>
 * Base class for {@link org.codehaus.groovy.ast.ClassCodeVisitorSupport} descendants. This class is used in GContracts
 * as root class for all code visitors directly used by global AST transformations.
 * </p>
 *
 * @see org.codehaus.groovy.ast.ClassCodeVisitorSupport
 *
 * @author andre.steingress@gmail.com
 */
public abstract class BaseVisitor extends ClassCodeVisitorSupport {

    protected static final String CLOSURE_ATTRIBUTE_NAME = "value";

    protected final SourceUnit sourceUnit;
    protected final ReaderSource source;

    public BaseVisitor(final SourceUnit sourceUnit, final ReaderSource source) {
        this.sourceUnit = sourceUnit;
        this.source = source;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }
}
