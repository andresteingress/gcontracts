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
package org.gcontracts.annotations;

import org.gcontracts.annotations.meta.AnnotationProcessorImplementation;
import org.gcontracts.annotations.meta.Postcondition;
import org.gcontracts.common.impl.EnsuresAnnotationProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Represents a <b>method postcondition</b>.
 * </p>
 * <p>
 * A postcondition is a condition that is guaranteed to be fulfilled by suppliers.
 * </p>
 * <p>
 * A method's postcondition is executed <i>after</i> a method call
 * has finished. A successor's postcondition strengthens the postcondition of its parent class, e.g. if A.someMethod
 * declares a postcondition and B.someMethod overrides the method the postconditions are combined with a boolean AND.
 * </p>
 * <p>
 * Compared to pre-conditions, postcondition annotation closures are optionally called with two additional
 * closure arguments: <tt>result</tt> and <tt>old</tt>.
 * </p>
 * <p>
 * <tt>result</tt> is available if the corresponding method has a non-void return-type and holds the
 * result of the method call. Be aware that modifying the internal state of a reference type can lead
 * to side-effects. GContracts does not keep track of any sort of modifications, neither any conversion to
 * immutability.
 * </p>
 * <p>
 * <tt>old</tt> is available in every postcondition. It is a {@link java.util.Map} which holds the values
 * of value types and {@link Cloneable} types before the method has been executed.
 * </p>
 * <p>
 * Examples:
 *
 * Accessing the <tt>result</tt> closure parameter:
 * 
 * <pre>
 *   &#064;Ensures({ result -> result != argument1 })
 *   def T someOperation(def argument1, def argument2)  {
 *     ...
 *   }
 * </pre>
 * 
 * Accessing the <tt>old</tt> closure parameter:
 * 
 * <pre>
 *   &#064;Ensures({ old -> old.counter + 1 == counter })
 *   def T someOperation(def argument1, def argument2)  {
 *     ...
 *   }
 * </pre>
 * </p>
 *
 * @author ast
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})

@Postcondition
@AnnotationProcessorImplementation(EnsuresAnnotationProcessor.class)
public @interface Ensures {
    public abstract Class value();
}