package org.gcontracts.spock

import org.junit.Test
import org.gcontracts.PreconditionViolation
import org.gcontracts.tests.basic.BaseTestClass
import org.junit.Ignore

/**
 * @author me@andresteingress.com
 */
@Ignore
class SpockIntegrationTests extends BaseTestClass {

    @Test void spec_with_requires()  {

        def source = """
                import spock.lang.*
                import org.gcontracts.annotations.*

                class FileBeanSpec extends Specification {

                  @Requires({ dir && file && path })
                  def test( String dir, String file, String path )
                  {
                      println 'test'
                  }
                }
             """

        shouldFail PreconditionViolation, {
            def clazz = add_class_to_classpath(source)
            def fileBeanSpec = clazz.newInstance()

            fileBeanSpec.test('', '', '')
        }
    }
}
