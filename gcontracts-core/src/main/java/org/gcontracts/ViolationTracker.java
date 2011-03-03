package org.gcontracts;

import org.gcontracts.util.Validate;

import java.util.TreeMap;

/**
 * A violation tracker is used to keep a list of pre-, post-condition or class-invariant
 * violations.
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
