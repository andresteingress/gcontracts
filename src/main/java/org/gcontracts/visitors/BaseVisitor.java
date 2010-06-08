package org.gcontracts.visitors;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.control.io.ReaderSource;
import org.gcontracts.injection.ClosureToSourceConverter;

/**
 * @author andre.steingress@gmail.com
 */
public abstract class BaseVisitor {

    public static final String INVARIANT_CLOSURE_PREFIX = "$invariant$";

    protected final ReaderSource source;
    protected final ClosureToSourceConverter closureToSourceConverter;

    public BaseVisitor(final ReaderSource source)  {
        this.source = source;
        this.closureToSourceConverter = new ClosureToSourceConverter();
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the field name of the invariant closure field of the given <tt>classNode</tt>
     */
    public String getInvariantClosureFieldName(final ClassNode classNode)  {
        return INVARIANT_CLOSURE_PREFIX + classNode.getNameWithoutPackage();
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the {@link org.codehaus.groovy.ast.FieldNode} which contains the invariant closure of the given <tt>classNode</tt>
     */
    public FieldNode getInvariantClosureFieldNode(final ClassNode classNode)  {
        return classNode.getField(getInvariantClosureFieldName(classNode));
    }
}
