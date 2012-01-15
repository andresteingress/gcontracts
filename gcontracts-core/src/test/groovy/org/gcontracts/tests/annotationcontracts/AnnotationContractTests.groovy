package org.gcontracts.tests.annotationcontracts

import org.junit.Test
import static junit.framework.Assert.assertNotNull
import static junit.framework.Assert.fail
import org.gcontracts.PreconditionViolation
import org.gcontracts.PostconditionViolation

/**
 * @author ast
 */
class AnnotationContractTests {

    @Test(expected = PreconditionViolation.class)
    void single_notnull_parameter() {

        def source_anno_parameter = '''
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
        def source_parameter = '''
    @Contracted
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        def method(@NotNull param) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno_parameter)
        Class clz = loader.parseClass(source_parameter)
        assertNotNull(clz)

        def tester = clz.newInstance()

        tester.method(null)
    }

    @Test(expected = PostconditionViolation.class)
    void single_notnull_parameter_postcondition() {

        def source_anno_parameter = '''
    package tests

    import org.gcontracts.annotations.*
    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Postcondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''
        def source_parameter = '''
    @Contracted
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        def method(@NotNull param) { println "test" }
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno_parameter)
        Class clz = loader.parseClass(source_parameter)
        assertNotNull(clz)

        def tester = clz.newInstance()

        tester.method(null)
    }

    @Test(expected = PreconditionViolation.class)
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
    @Contracted
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

        tester.method(null, null)
    }

    @Test(expected = PreconditionViolation.class)
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
    package tests

    class Tester {
        def Tester(@NotNull param1, @NotNull param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno)
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        clz.newInstance([null,2] as Object[])
    }

    @Test(expected = PreconditionViolation.class)
    void requires_method() {

        def source = '''
    @Contracted
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        @Requires({ param1 != null && param2 != null })
        def method(param1, param2) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source)
        assertNotNull(clz)

        def tester = clz.newInstance()
        tester.method(null, null)
    }

    @Test(expected = PreconditionViolation.class)
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
    @Contracted
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

        def tester = clz.newInstance()
        tester.method(null, null)
    }

    @Test
    void annotation_contract_for_method_precondition() {


        def source_anno_method = '''
    package tests

    import org.gcontracts.annotations.*
    import org.gcontracts.annotations.meta.*
    import java.lang.annotation.*

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)

    @Precondition
    @AnnotationContract({ param1 != null })
    public @interface NotNull {}
'''

        def source_method = '''
    @Contracted
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        @NotNull
        def method(def param1) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        Class clz = loader.parseClass(source_anno_method)
        assertNotNull(clz)

        clz = loader.parseClass(source_method)

        try  {
            def tester = clz.newInstance()
            tester.method(null)

        } catch (PreconditionViolation pv) {
            return
        }

        fail("PreconditionViolation must have been thrown")
    }

    @Test(expected = PreconditionViolation.class)
    void single_notnull_parameter_without_retention_and_target() {

        def source_anno_parameter = '''
    package tests

    import org.gcontracts.annotations.*
    import org.gcontracts.annotations.meta.*

    @Precondition
    @AnnotationContract({ it != null })
    public @interface NotNull {}
'''
        def source_parameter = '''
    @Contracted
    package tests

    import org.gcontracts.annotations.*

    class Tester {

        def method(@NotNull param) {}
    }'''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno_parameter)
        Class clz = loader.parseClass(source_parameter)
        assertNotNull(clz)

        def tester = clz.newInstance()

        tester.method(null)
    }

    @Test(expected = PreconditionViolation.class)
    void single_notnull_parameter_in_interface() {

        def source_anno_parameter = '''
        package tests

        import org.gcontracts.annotations.*
        import org.gcontracts.annotations.meta.*

        @Precondition
        @AnnotationContract({ it != null })
        public @interface NotNull {}
    '''
        def source_interface = '''
        @Contracted
        package tests

        import org.gcontracts.annotations.*

        interface Tester {
            def method(@NotNull param)
        }

        '''

        def source_class = '''
        @Contracted
        package tests

        import org.gcontracts.annotations.*

        class TesterImpl implements Tester {
            def method(def param) {  ;; }
        }

        '''

        GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
        loader.parseClass(source_anno_parameter)
        loader.parseClass(source_interface)
        def clz = loader.parseClass(source_class)

        assertNotNull(clz)

        def tester = clz.newInstance()

        tester.method(null)
    }
}
