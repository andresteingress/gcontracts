package org.gcontracts.common.spi;

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

    private List<MethodNode> preconditionMethodNodes;
    private List<MethodNode> postconditionMethodNodes;

    public ProcessingContextInformation(ReaderSource source, boolean preconditions, boolean postconditions)  {
        // Validate.notNull(source);

        this.source = source;
        this.preconditionsEnabled = preconditions;
        this.postconditionsEnabled = postconditions;

        this.preconditionMethodNodes = new ArrayList<MethodNode>();
        this.postconditionMethodNodes = new ArrayList<MethodNode>();
    }

    public boolean isPreconditionsEnabled() { return preconditionsEnabled; }
    public boolean isPostconditionsEnabled() { return postconditionsEnabled; }
    public List<MethodNode> preconditionMethodNodes() { return preconditionMethodNodes; }
    public List<MethodNode> postconditionMethodNodes() { return postconditionMethodNodes; }
    public ReaderSource readerSource() { return source; }


}
