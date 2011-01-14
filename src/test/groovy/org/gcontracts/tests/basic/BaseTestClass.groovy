package org.gcontracts.tests.basic

import org.codehaus.groovy.control.CompilePhase
import org.gcontracts.ast.GContractsASTTransformation
import groovy.text.GStringTemplateEngine

/**
 * @author andre.steingress@gmail.com
 */
class BaseTestClass extends GroovyTestCase {

  private groovy.text.TemplateEngine templateEngine
  private ASTTransformationTestsGroovyClassLoader loader;

  protected void setUp() {
    super.setUp();

    templateEngine = new GStringTemplateEngine()

    loader = new ASTTransformationTestsGroovyClassLoader(getClass().getClassLoader(), [
            new GContractsASTTransformation()
    ], CompilePhase.SEMANTIC_ANALYSIS)
  }

  String createSourceCodeForTemplate(final String template, final Map binding)  {
    templateEngine.createTemplate(template).make(binding).toString()
  }

  def create_instance_of(final String sourceCode)  {
    return create_instance_of(sourceCode, new Object[0])
  }

  def create_instance_of(final String sourceCode, def constructor_args)  {
    
    def clazz = add_class_to_classpath(sourceCode)

    return clazz.newInstance(constructor_args as Object[])
  }

  def add_class_to_classpath(final String sourceCode)  {
    loader.parseClass(sourceCode)
  }

}
