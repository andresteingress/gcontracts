package org.gcontracts.ast.visitor;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;

/**
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
