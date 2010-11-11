package org.gcontracts;

/**
 * Thrown whenever a precondition violation occurs.
 *
 * @author andre.steingress@gmail.com
 */
public class PreconditionViolation extends AssertionError {

    public PreconditionViolation() {
    }

    public PreconditionViolation(Object o) {
        super(o);
    }

    public PreconditionViolation(boolean b) {
        super(b);
    }

    public PreconditionViolation(char c) {
        super(c);
    }

    public PreconditionViolation(int i) {
        super(i);
    }

    public PreconditionViolation(long l) {
        super(l);
    }

    public PreconditionViolation(float v) {
        super(v);
    }

    public PreconditionViolation(double v) {
        super(v);
    }
}
