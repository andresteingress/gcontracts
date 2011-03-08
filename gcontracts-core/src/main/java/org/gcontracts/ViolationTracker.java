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
package org.gcontracts;

import org.gcontracts.util.Validate;

import java.util.TreeMap;

/**
 * <p>
 * A violation tracker is used to keep a list of pre-, post-condition or class-invariant
 * violations in chronological order. This is necessary to evaluate all parts of a pre- or postcondition, and still
 * being able to rethrow assertion errors.
 * </p>
 *
 * @author andre.steingress@gmail.com
 */
public class ViolationTracker {

    public static final ThreadLocal<ViolationTracker> INSTANCE = new ThreadLocal<ViolationTracker>();

    public static void init()  {
        INSTANCE.set(new ViolationTracker());
    }

    public static void deinit()  {
        INSTANCE.remove();
    }

    public static boolean violationsOccured()  {
        return INSTANCE.get().hasViolations();
    }

    public static void rethrowFirst()  {
        throw INSTANCE.get().first();
    }

    public static void rethrowLast()  {
        throw INSTANCE.get().last();
    }

    private TreeMap<Long, AssertionViolation> violations = new TreeMap<Long, AssertionViolation>();

    public void track(final AssertionViolation assertionViolation)  {
        Validate.notNull(assertionViolation);

        violations.put(System.nanoTime(), assertionViolation);
    }

    public boolean hasViolations()  {
        return violations.size() > 0;
    }

    public AssertionViolation first()  {
        return violations.firstEntry().getValue();
    }

    public AssertionViolation last()  {
        return violations.lastEntry().getValue();
    }
}
