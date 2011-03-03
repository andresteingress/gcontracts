/**
 * Copyright (c) 2011, Andre Steingress
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

import org.codehaus.groovy.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods for reading/getting {@link org.codehaus.groovy.ast.AnnotationNode} instances.
 *
 * @author ast
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
     * Gets the next {@link org.codehaus.groovy.ast.AnnotationNode} instance in the inheritance line which is annotated
     * with the given Annotation class <tt>anno</tt>.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} to check for the annotation
     * @param anno the annotation to watch out for
     * @return the next {@link org.codehaus.groovy.ast.AnnotationNode} in the inheritance line, or <tt>null</tt>
     */
    public static List<AnnotationNode> getAnnotationNodeInHierarchyWithMetaAnnotation(ClassNode type, ClassNode anno)  {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>();
        for (AnnotationNode annotation : type.getAnnotations())  {
            if (annotation.getClassNode().getAnnotations(anno).size() > 0)  {
                result.add(annotation);
            }
        }

        if (result.isEmpty() && type.getSuperClass() != null)  {
           return getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), anno);
        } else  {
           return result;
        }
    }

    public static List<AnnotationNode> getAnnotationNodeInHierarchyWithMetaAnnotation(ClassNode type, MethodNode originMethodNode, ClassNode anno)  {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>();

        while (type != null)  {
            MethodNode methodNode = type.getMethod(originMethodNode.getName(), originMethodNode.getParameters());
            if (methodNode != null) {
                for (AnnotationNode annotation : methodNode.getAnnotations())  {
                    if (annotation.getClassNode().getAnnotations(anno).size() > 0)  {
                        result.add(annotation);
                    }
                }

                if (result.size() > 0) return result;
            }

            type = type.getSuperClass();
        }

        return result;
    }

    /**
     * Loads all annotation nodes of the given {@link org.codehaus.groovy.ast.AnnotatedNode} instance which are marked
     * with the annotation <tt>metaAnnotationClassName</tt>.
     *
     * @param annotatedNode an {@link org.codehaus.groovy.ast.AnnotatedNode} from which the annotations are checked
     * @param metaAnnotationClassName the name of the meta annotation
     * @return a list of {@link AnnotationNode} instances which implement the given <tt>metaAnnotationClass</tt>
     */
    public static List<AnnotationNode> hasMetaAnnotations(AnnotatedNode annotatedNode, String metaAnnotationClassName)  {

        ArrayList<AnnotationNode> result = new ArrayList<AnnotationNode>();

        for (AnnotationNode annotationNode : annotatedNode.getAnnotations())  {
            if (!annotationNode.getClassNode().getName().startsWith("org.gcontracts")) continue;

            // is the annotation marked with the given meta annotation?
            List<AnnotationNode> metaAnnotations = annotationNode.getClassNode().getAnnotations(ClassHelper.makeWithoutCaching(metaAnnotationClassName));
            if (metaAnnotations.isEmpty())  {
                metaAnnotations = hasMetaAnnotations(annotationNode.getClassNode(), metaAnnotationClassName);
            }

            if (metaAnnotations.size() > 0) result.add(annotationNode);
        }
        return result;
    }
}
