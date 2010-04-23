package org.gcontracts.injection;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;

/**
 * Implemented by components that rewrite {@link org.codehaus.groovy.ast.ClassNode} instances.
 *
 * @see org.codehaus.groovy.ast.ClassNode
 *
 * @author andre.steingress@gmail.com
 */
public abstract class Injector {

    private static final String INVARIANT_CLOSURE_PREFIX = "$invariant$";

    /**
     * Rewrites the current {@link org.codehaus.groovy.ast.ClassNode}.
     */
    public abstract void rewrite();

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