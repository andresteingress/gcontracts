package org.gcontracts.generation;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;

/**
 * <p>
 * Functions in this class are used to determine whether a certain AST node fulfills certain assertion
 * requirements. E.g. whether a class node is a class invariant candidate or not.
 * </p>
 *
 * @author andre.steingress@gmail.com
 */
public class CandidateChecks {

        /**
     * Decides whether the given <tt>constructorNode</tt> is a candidate for class invariant injection.
     *
     * @param constructorNode the {@link org.codehaus.groovy.ast.ConstructorNode} to check
     * @return whether the <tt>constructorNode</tt> is a candidate for injecting the class invariant or not
     */
    public static boolean isClassInvariantCandidate(final ConstructorNode constructorNode)  {
        return constructorNode != null &&
                constructorNode.isPublic() && !constructorNode.isStatic() && !constructorNode.isStaticConstructor();
    }

    /**
     * Decides whether the given <tt>methodNode</tt> is a candidate for class invariant injection.
     *
     * @param methodNode the {@link org.codehaus.groovy.ast.MethodNode} to check
     * @return whether the <tt>methodNode</tt> is a candidate for injecting the class invariant or not
     */
    public static boolean isClassInvariantCandidate(final MethodNode methodNode)  {
        return methodNode != null &&
                methodNode.isPublic() && !methodNode.isStatic() && !methodNode.isStaticConstructor() && !methodNode.isAbstract();
    }

    /**
     * Decides whether the given <tt>propertyNode</tt> is a candidate for class invariant injection.
     *
     * @param propertyNode the {@link org.codehaus.groovy.ast.PropertyNode} to check
     * @return whether the <tt>propertyNode</tt> is a candidate for injecting the class invariant or not
     */
    public static boolean isClassInvariantCandidate(final PropertyNode propertyNode)  {
        return propertyNode != null &&
                propertyNode.isPublic() && !propertyNode.isStatic() && !propertyNode.isInStaticContext() && !propertyNode.isClosureSharedVariable();
    }

    /**
     * Decides whether the given <tt>method</tt> is a candidate for a pre- or postcondition.
     *
     * @param type the current {@link org.codehaus.groovy.ast.ClassNode}
     * @param method the {@link org.codehaus.groovy.ast.MethodNode} to check for pre- or postcondition compliance
     * @return whether the given {@link org.codehaus.groovy.ast.MethodNode} is a candidate for pre- or postconditions 
     */
    public static boolean isPreOrPostconditionCandidate(final ClassNode type, final MethodNode method)  {
        if (method.isSynthetic() || method.isStatic() || method.isStaticConstructor() || method.isAbstract() || !method.isPublic()) return false;
        if (method.hasDefaultValue() || method.hasAnnotationDefault()) return false;
        if (method.getDeclaringClass() != type) return false;

        return true;
    }
}
