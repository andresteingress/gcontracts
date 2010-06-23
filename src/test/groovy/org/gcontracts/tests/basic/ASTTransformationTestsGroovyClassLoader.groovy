package org.gcontracts.tests.basic

import java.security.CodeSource
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.ast.TestHarnessOperation
import org.codehaus.groovy.transform.ASTTransformation

/**
 * @author andre.steingress@gmail.com
 */
class ASTTransformationTestsGroovyClassLoader extends GroovyClassLoader {

    private List transformations
    private CompilePhase phase

    def ASTTransformationTestsGroovyClassLoader(ClassLoader parent, List transformations, CompilePhase phase) {
        super(parent)

        this.transformations = transformations
        this.phase = phase
    }

    @Override
    protected CompilationUnit createCompilationUnit(CompilerConfiguration config, CodeSource codeSource) {

        CompilationUnit cu = super.createCompilationUnit(config, codeSource)
        for (ASTTransformation transformation : transformations)  {
          cu.addPhaseOperation(new TestHarnessOperation(transformation), phase.getPhaseNumber())
        }
      
        return cu
    }
}