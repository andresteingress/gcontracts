package org.gcontracts.ast.visitor

import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.gcontracts.tests.basic.BaseTestClass

 /**
 * @author andre.steingress@gmail.com
 */
class AnnotationProcessingASTTransformationsVisitorTests extends BaseTestClass {

    AstStringCompiler astStringCompiler = new AstStringCompiler()

    def void test_single_notnull_parameter() {

        def source = '''
    import org.gcontracts.annotations.common.*

    class Tester {

        def method(@NotNull param) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        def tester = clz.newInstance()

        try  { tester.method(null) } catch (AssertionError ae) {}
    }

    def void test_multiple_notnull_parameters() {

        def source = '''
    import org.gcontracts.annotations.common.*

    class Tester {

        def method(@NotNull param1, @NotNull param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        def tester = clz.newInstance()

        try  { tester.method(null, []) } catch (AssertionError ae) {}
        try  { tester.method([], null) } catch (AssertionError ae) {}
        try  { tester.method(null, null) } catch (AssertionError ae) {}
    }

    def void test_constructor_params() {

        def source = '''
    import org.gcontracts.annotations.common.*

    class Tester {

        def Tester(@NotNull param1, @NotNull param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        try  { clz.newInstance([null,2] as Object[]) } catch (AssertionError ae) { return }

        fail("AssertionError must have been thrown")
    }
}
