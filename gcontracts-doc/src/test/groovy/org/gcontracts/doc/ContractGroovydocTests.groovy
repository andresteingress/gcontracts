package org.gcontracts.doc

import org.junit.Test

/**
 * @author ast
 */
class ContractGroovydocTests {

    @Test
    void execute_on_example_package()  {

        def ant = new AntBuilder()
        ant.taskdef(name: "groovydoc", classname: "org.gcontracts.doc.ContractGroovyDoc")

        ant.groovydoc(
            destdir      : "gcontracts-doc/out/test",
            sourcepath   : "gcontracts-doc/src/test/groovy",
            packagenames : "**.*",
            use          : "true",
            windowtitle  : "Title",
            doctitle     : "Doctitle",
            header       : "Header",
            footer       : "Docfooter",
            overview     : "src/main/overview.html",
            private      : "false",
	{
	   link(packages:"java.,org.xml.,javax.,org.xml.",href:"http://download.oracle.com/javase/6/docs/api")
	   link(packages:"groovy.,org.codehaus.groovy.",  href:"http://groovy.codehaus.org/api")
	   link(packages:"org.apache.tools.ant.",         href:"http://evgeny-goldin.org/javadoc/ant/api")
	   link(packages:"org.junit.,junit.framework.",   href:"http://kentbeck.github.com/junit/javadoc/latest")
	   link(packages:"org.codehaus.gmaven.",          href:"http://evgeny-goldin.org/javadoc/gmaven")
	}
)

    }

}
