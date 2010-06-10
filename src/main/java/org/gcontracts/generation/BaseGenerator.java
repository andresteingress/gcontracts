package org.gcontracts.generation;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.control.io.ReaderSource;

/**
 * <p>
 * Base class for GContracts code generators.
 * </p>
 *
 * @author andre.steingress@gmail.com
 */
public abstract class BaseGenerator {

    public static final String INVARIANT_CLOSURE_PREFIX = "$invariant$";

    protected final ReaderSource source;

    public BaseGenerator(final ReaderSource source)  {
        this.source = source;
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the field name of the invariant closure field of the given <tt>classNode</tt>
     */
    public static String getInvariantClosureFieldName(final ClassNode classNode)  {
        return INVARIANT_CLOSURE_PREFIX + classNode.getNameWithoutPackage();
    }

    /**
     * @param classNode the {@link org.codehaus.groovy.ast.ClassNode} used to look up the invariant closure field
     *
     * @return the {@link org.codehaus.groovy.ast.FieldNode} which contains the invariant closure of the given <tt>classNode</tt>
     */
    public static FieldNode getInvariantClosureFieldNode(final ClassNode classNode)  {
        return classNode.getField(getInvariantClosureFieldName(classNode));
    }
}
