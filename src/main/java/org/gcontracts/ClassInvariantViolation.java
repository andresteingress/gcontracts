package org.gcontracts;

/**
 * Thrown whenever a class invariant violation occurs.
 *
 * @author andre.steingress@gmail.com
 */
public class ClassInvariantViolation extends AssertionError {

    public ClassInvariantViolation() {
    }

    public ClassInvariantViolation(Object o) {
        super(o);
    }

    public ClassInvariantViolation(boolean b) {
        super(b);
    }

    public ClassInvariantViolation(char c) {
        super(c);
    }

    public ClassInvariantViolation(int i) {
        super(i);
    }

    public ClassInvariantViolation(long l) {
        super(l);
    }

    public ClassInvariantViolation(float v) {
        super(v);
    }

    public ClassInvariantViolation(double v) {
        super(v);
    }
}
