package org.gcontracts.tests.annotationcontracts

import org.codehaus.groovy.ast.builder.AstStringCompiler
import org.junit.Test
import static junit.framework.Assert.assertNotNull
import static junit.framework.Assert.fail

 /**
 * @author ast
 */
class AnnotationContractTests {

    AstStringCompiler astStringCompiler = new AstStringCompiler()

    @Test
    void single_notnull_parameter() {

        def source_anno = '''
    package tests

    import org.gcontracts.annotations.*
    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        def method(@NotNull param) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno)
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        def tester = clz.newInstance()

        try  { tester.method(null) } catch (AssertionError ae) {}
    }

    @Test
    void multiple_notnull_parameters() {

        def source_anno = '''
    package tests

    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {
        def method(@NotNull param1, @NotNull param2) { println "BEEN CALLED" }
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno)
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        def tester = clz.newInstance()

        try  { tester.method(null, []) } catch (AssertionError ae) {}
        try  { tester.method([], null) } catch (AssertionError ae) {}
        try  { tester.method(null, null) } catch (AssertionError ae) {}
    }

    void test_constructor_params() {

        def source_anno = '''
    package tests

    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''

        def source = '''
    @AssertionsEnabled
    package tests

    class Tester {
        def Tester(@NotNull param1, @NotNull param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno)
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        try  { clz.newInstance([null,2] as Object[]) } catch (AssertionError ae) { return }

        fail("AssertionError must have been thrown")
    }

    @Test
    void requires_method() {

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        @Requires({ param1 != null && param2 != null })
        def method(param1, param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        try  {
            def tester = clz.newInstance()
            tester.method(null, null)

        } catch (AssertionError ae) {
            ae.printStackTrace()
            return }

        fail("AssertionError must have been thrown")
    }

    @Test
    void default_requires_method() {

        def source_anno = '''
    package tests

    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        def method(param1, param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno)
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

    }

    @Test
    void requires_method_with_not_null_parameter() {

        def source_custom_anno = '''
    package tests

    import org.gcontracts.annotations.*
    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}

'''

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        @Requires({ param1 > 1 })
        def method(@NotNull param1, param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_custom_anno)

        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        try {
            def tester = clz.newInstance()
            tester.method(null, null)
        } catch (AssertionError ae) {
            ae.printStackTrace()
            return
        }

        fail("AssertionError must have been thrown")

    }

    @Test
    void default_ensures_method() {

        def source_anno = '''
    package tests

    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        def method(param1, param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno)
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

    }

    @Test
    void ensures_method() {

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        @Ensures({ param1 != null && param2 != null })
        def method(param1, param2) {
            def i = 1 + 1;
            return "";
        }
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        try  {
            def tester = clz.newInstance()
            tester.method(null, null)

        } catch (AssertionError ae) {
            ae.printStackTrace()
            return
        }

        fail("AssertionError must have been thrown")
    }

    @Test
    void class_invariant() {

        def source = '''
    @AssertionsEnabled
    package tests

    import org.gcontracts.annotations.*

    @Invariant({ prop != null })
    class Tester {

        def prop = ""

        def method() {
            prop = null
        }
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        try  {
            def tester = clz.newInstance()
            tester.method()

        } catch (AssertionError ae) {
            ae.printStackTrace()
            return }

        fail("AssertionError must have been thrown")
    }
}
