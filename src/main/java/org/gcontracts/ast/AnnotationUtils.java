package org.gcontracts.ast;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;

/**
 * Helper methods for reading/getting {@link org.codehaus.groovy.ast.AnnotationNode} instances.
 *
 * @author andre.steingress@gmail.com
 */
public class AnnotationUtils {

    /**
     * Gets the next {@link org.codehaus.groovy.ast.ClassNode} in the inheritance line which is annotated
     * with the given Annotation class.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} to check for the annotation
     * @param anno the annotation to watch out for
     * @return the next {@link org.codehaus.groovy.ast.ClassNode} in the inheritance line, or <tt>null</tt>
     */
    public static ClassNode getNextClassNodeWithAnnotation(ClassNode type, Class anno)  {
        for (AnnotationNode annotation : type.getAnnotations())  {
            if (annotation.getClassNode().getTypeClass() == anno)  {
                return type;
            }
        }

        if (type.getSuperClass() != null) return getNextClassNodeWithAnnotation(type.getSuperClass(), anno); else return null;
    }
}
