package org.gcontracts.common.spi;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.io.ReaderSource;

import java.util.ArrayList;
import java.util.List;

/**
 * @author andre.steingress@gmail.com
 */
public class ProcessingContextInformation {

    private ReaderSource source;

    private boolean preconditionsEnabled;
    private boolean postconditionsEnabled;
    private boolean classInvariantsEnabled;

    private List<MethodNode> preconditionMethodNodes;
    private List<MethodNode> postconditionMethodNodes;
    private List<ClassNode> classInvariantClassNodes;

    public ProcessingContextInformation(ReaderSource source, boolean classInvariants, boolean preconditions, boolean postconditions)  {
        // Validate.notNull(source);

        this.source = source;
        this.preconditionsEnabled = preconditions;
        this.postconditionsEnabled = postconditions;
        this.classInvariantsEnabled = classInvariants;

        this.preconditionMethodNodes = new ArrayList<MethodNode>();
        this.postconditionMethodNodes = new ArrayList<MethodNode>();
        this.classInvariantClassNodes = new ArrayList<ClassNode>();
    }

    public boolean isPreconditionsEnabled() { return preconditionsEnabled; }
    public boolean isPostconditionsEnabled() { return postconditionsEnabled; }
    public boolean isClassInvariantsEnabled() { return classInvariantsEnabled; }
    public List<MethodNode> preconditionMethodNodes() { return preconditionMethodNodes; }
    public List<MethodNode> postconditionMethodNodes() { return postconditionMethodNodes; }
    public List<ClassNode> classInvariantClassNodes() { return classInvariantClassNodes; }
    public ReaderSource readerSource() { return source; }


}
