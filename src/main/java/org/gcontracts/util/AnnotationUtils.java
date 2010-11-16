/**
 * Copyright (c) 2010, gcontracts@me.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gcontracts.util;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

/**
 * Helper methods for reading/getting {@link org.codehaus.groovy.ast.AnnotationNode} instances.
 *
 * @author andre.steingress@gmail.com
 */
public class AnnotationUtils {

    /**
     * Checks whether the given {@link org.codehaus.groovy.ast.ClassNode} is annotated
     * with an annotations of the given package or full annotatedNode name.
     *
     * @param annotatedNode the {@link org.codehaus.groovy.ast.AnnotatedNode} to search for the given annotation
     * @param typeOrPackageName can either be a part of the package or the complete annotation class name
     * @return <tt>true</tt> if an annotation was found, <tt>false</tt> otherwise
     */
    public static boolean hasAnnotationOfType(AnnotatedNode annotatedNode, String typeOrPackageName)  {
        for (AnnotationNode annotation: annotatedNode.getAnnotations())  {
            if (annotation.getClassNode().getName().startsWith(typeOrPackageName))  {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the next {@link org.codehaus.groovy.ast.ClassNode} in the inheritance line which is annotated
     * with the given Annotation class.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} to check for the annotation
     * @param anno the annotation to watch out for
     * @return the next {@link org.codehaus.groovy.ast.ClassNode} in the inheritance line, or <tt>null</tt>
     */
    public static ClassNode getClassNodeInHierarchyWithAnnotation(ClassNode type, Class anno)  {
        for (AnnotationNode annotation : type.getAnnotations())  {
            if (annotation.getClassNode().getName().equals(anno.getName()))  {
                return type;
            }
        }

        if (type.getSuperClass() != null) return getClassNodeInHierarchyWithAnnotation(type.getSuperClass(), anno); else return null;
    }

    /**
     * Gets the next {@link org.codehaus.groovy.ast.MethodNode} in the inheritance line which is annotated
     * with the given Annotation class.
     *
     * @param methodNode the {@link org.codehaus.groovy.ast.ClassNode} to check for the annotation
     * @param anno the annotation to watch out for
     * @return the next {@link org.codehaus.groovy.ast.MethodNode} in the inheritance line, or <tt>null</tt>
     */
    public static MethodNode getMethodNodeInHierarchyWithAnnotation(MethodNode methodNode, Class anno)  {
        final ClassNode type = methodNode.getDeclaringClass();
        if (type.getSuperClass() == null) return null;

        final ClassNode superClass = type.getSuperClass();
        final MethodNode superMethod = superClass.getMethod(methodNode.getName(), methodNode.getParameters());
        if (superMethod == null) return null;

        for (AnnotationNode annotation : superMethod.getAnnotations())  {
            if (annotation.getClassNode().getName().equals(anno.getName()))  {
                return superMethod;
            }
        }
        
        return getMethodNodeInHierarchyWithAnnotation(superMethod, anno);
    }
}
