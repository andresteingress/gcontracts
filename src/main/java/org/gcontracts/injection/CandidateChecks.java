package org.gcontracts.injection;

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;

/**
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
}
