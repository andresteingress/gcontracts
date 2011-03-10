package org.gcontracts.util

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.gcontracts.annotations.meta.Precondition
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Test
import static org.junit.Assert.assertEquals

/**
 * @author ast
 */
class AnnotationUtilsTests extends BaseTestClass {

    def source = '''
    @Contracted
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        @Requires({ param != null })
        def method(def param) {}

    }'''

    @Test void find_annotations_with_meta_annos() {
        AstBuilder astBuilder  = new AstBuilder()
        def astNodes = astBuilder.buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, source)

        ClassNode classNode = astNodes[1]
        MethodNode methodNode = classNode.getMethod("method", [new Parameter(ClassHelper.makeWithoutCaching("java.lang.Object"), "param")] as Parameter[] )

        def annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, Precondition.class.getName())
        assertEquals(1, annotationNodes.size())
    }
}
