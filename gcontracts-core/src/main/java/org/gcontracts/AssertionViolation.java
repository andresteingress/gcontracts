package org.gcontracts;

/**
 * @author andre.steingress@gmail.com
 */
public abstract class AssertionViolation extends AssertionError {

    protected AssertionViolation() {
        ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(Object o) {
        super(o);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(boolean b) {
        super(b);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(char c) {
        super(c);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(int i) {
        super(i);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(long l) {
        super(l);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(float v) {
        super(v);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }

    protected AssertionViolation(double v) {
        super(v);
        if (ViolationTracker.INSTANCE.get() != null) ViolationTracker.INSTANCE.get().track(this);
    }
}
