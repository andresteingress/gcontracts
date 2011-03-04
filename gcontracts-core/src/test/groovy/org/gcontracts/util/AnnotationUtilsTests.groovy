package org.gcontracts.util

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.codehaus.groovy.control.CompilePhase
import org.gcontracts.annotations.meta.ContractElement
import org.gcontracts.tests.basic.BaseTestClass

 /**
 * @author ast
 */
class AnnotationUtilsTests extends BaseTestClass {

    def source = '''
    import org.gcontracts.annotations.*

    class Tester {

        @Requires({ param != null })
        def method(def param) {}

    }'''

    void test_find_annotations_with_meta_annos() {
        AstStringCompiler astStringCompiler = new AstStringCompiler()
        def astNodes = astStringCompiler.compile(source, CompilePhase.SEMANTIC_ANALYSIS, false)

        ClassNode classNode = astNodes[1]
        MethodNode methodNode = classNode.getMethod("method", [new Parameter(ClassHelper.makeWithoutCaching("java.lang.Object"), "param")] as Parameter[] )

        def annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, ContractElement.class.getName())
        assertEquals(1, annotationNodes.size())
    }
}
