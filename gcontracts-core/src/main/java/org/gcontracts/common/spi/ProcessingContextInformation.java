package org.gcontracts.common.spi;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.gcontracts.util.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andre.steingress@gmail.com
 */
public class ProcessingContextInformation {

    private SourceUnit sourceUnit;
    private ReaderSource source;

    private boolean constructorAssertionsEnabled = true;
    private boolean preconditionsEnabled = true;
    private boolean postconditionsEnabled = true;
    private boolean classInvariantsEnabled = true;

    private List<MethodNode> preconditionMethodNodes = new ArrayList<MethodNode>();
    private List<MethodNode> postconditionMethodNodes = new ArrayList<MethodNode>();
    private List<ClassNode> classInvariantClassNodes = new ArrayList<ClassNode>();

    private Map<String, Object> extra = new HashMap<String, Object>();

    public ProcessingContextInformation(SourceUnit sourceUnit, ReaderSource source)  {
        // Validate.notNull(source);

        this.sourceUnit = sourceUnit;
        this.source = source;
    }

    public void setConstructorAssertionsEnabled(boolean other) { constructorAssertionsEnabled = other; }
    public boolean isConstructorAssertionsEnabled() { return constructorAssertionsEnabled; }

    public boolean isPreconditionsEnabled() { return preconditionsEnabled; }
    public boolean isPostconditionsEnabled() { return postconditionsEnabled; }
    public boolean isClassInvariantsEnabled() { return classInvariantsEnabled; }
    public List<MethodNode> preconditionMethodNodes() { return preconditionMethodNodes; }
    public List<MethodNode> postconditionMethodNodes() { return postconditionMethodNodes; }
    public List<ClassNode> classInvariantClassNodes() { return classInvariantClassNodes; }
    public ReaderSource readerSource() { return source; }
    public SourceUnit sourceUnit() { return sourceUnit; }

    public void put(String key, Object value) {
        Validate.notNull(key);

        extra.put(key , value);
    }

    public Object get(String key)  {
        Validate.notNull(key);

        return extra.get(key);
    }

    public void addError(String msg, ASTNode expr) {
        int line = expr.getLineNumber();
        int col = expr.getColumnNumber();
        SourceUnit source = sourceUnit();
        source.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', line, col), source)
        );
    }
}
