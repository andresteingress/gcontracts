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
package org.gcontracts.ast;

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.ASTTransformation;

import java.lang.reflect.Field;

/**
 * base AST transformation encapsulating all common helper methods and implementing {@link org.codehaus.groovy.transform.ASTTransformation}.
 *
 * @see org.codehaus.groovy.transform.ASTTransformation
 *
 * @author andre.steingress@gmail.com
 */
public abstract class BaseASTTransformation implements ASTTransformation {

    /**
     * Reads the protected <tt>source1</tt> instance variable of {@link org.codehaus.groovy.control.SourceUnit}.
     *
     * @param unit the {@link org.codehaus.groovy.control.SourceUnit} to retrieve the {@link org.codehaus.groovy.control.io.ReaderSource} from
     * @return the {@link org.codehaus.groovy.control.io.ReaderSource} of the given <tt>unit</tt>.
     */
    protected ReaderSource getReaderSource(SourceUnit unit)  {

        try {
            Class sourceUnitClass = unit.getClass();

            while (sourceUnitClass != SourceUnit.class)  {
                sourceUnitClass = sourceUnitClass.getSuperclass();
            }

            Field field = sourceUnitClass.getDeclaredField("source1");
            field.setAccessible(true);

            return (ReaderSource) field.get(unit);
        } catch (Exception e) {
            return null;
        }
    }

}
