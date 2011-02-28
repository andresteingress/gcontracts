/**
 * Copyright (c) 2011, Andre Steingress
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1.) Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 * 2.) Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.) Neither the name of Andre Steingress nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gcontracts.common.spi;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.gcontracts.domain.Contract;
import org.gcontracts.util.Validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author andre.steingress@gmail.com
 */
public class ProcessingContextInformation {

    private Contract contract;
    private SourceUnit sourceUnit;
    private ReaderSource source;

    private boolean constructorAssertionsEnabled = true;
    private boolean preconditionsEnabled = true;
    private boolean postconditionsEnabled = true;
    private boolean classInvariantsEnabled = true;

    private Map<String, Object> extra = new HashMap<String, Object>();

    public ProcessingContextInformation(ClassNode classNode, SourceUnit sourceUnit, ReaderSource source)  {
        Validate.notNull(classNode);

        this.contract = new Contract(classNode);
        this.sourceUnit = sourceUnit;
        this.source = source;
    }

    public void setConstructorAssertionsEnabled(boolean other) { constructorAssertionsEnabled = other; }
    public boolean isConstructorAssertionsEnabled() { return constructorAssertionsEnabled; }

    public boolean isPreconditionsEnabled() { return preconditionsEnabled; }
    public boolean isPostconditionsEnabled() { return postconditionsEnabled; }
    public boolean isClassInvariantsEnabled() { return classInvariantsEnabled; }

    public Contract contract() { return contract; }
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
