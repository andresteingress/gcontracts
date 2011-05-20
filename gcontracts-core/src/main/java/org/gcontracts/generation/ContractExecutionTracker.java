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
package org.gcontracts.generation;

import java.util.HashSet;

/**
 * Keeps track of contract executions to avoid cyclic contract checks.
 *
 * @author ast
 */
public class ContractExecutionTracker {

    public static class ContractExecution {
        String className;
        String methodIdentifier;

        public ContractExecution(String className, String methodIdentifier)  {
            this.className = className;
            this.methodIdentifier = methodIdentifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContractExecution that = (ContractExecution) o;

            if (className != null ? !className.equals(that.className) : that.className != null) return false;
            if (methodIdentifier != null ? !methodIdentifier.equals(that.methodIdentifier) : that.methodIdentifier != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (methodIdentifier != null ? methodIdentifier.hashCode() : 0);
            return result;
        }
    }

    private static ThreadLocal<HashSet<ContractExecution>> executions = new ThreadLocal<HashSet<ContractExecution>>()  {

        @Override
        protected HashSet<ContractExecution> initialValue() {
            return new HashSet<ContractExecution>();
        }
    };

    public static boolean track(String className, String methodIdentifier)  {
        final ContractExecution ce = new ContractExecution(className, methodIdentifier);

        if (!executions.get().contains(ce))  {
            executions.get().add(ce);
            return true;
        }

        return false;
    }

    public static void clear() {
        executions.get().clear();
    }
}
