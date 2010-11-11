package org.gcontracts;

/**
 * Thrown whenever a postcondition violation occurs.
 *
 * @author andre.steingress@gmail.com
 */
public class PostconditionViolation extends AssertionError {

    public PostconditionViolation() {
    }

    public PostconditionViolation(Object o) {
        super(o);
    }

    public PostconditionViolation(boolean b) {
        super(b);
    }

    public PostconditionViolation(char c) {
        super(c);
    }

    public PostconditionViolation(int i) {
        super(i);
    }

    public PostconditionViolation(long l) {
        super(l);
    }

    public PostconditionViolation(float v) {
        super(v);
    }

    public PostconditionViolation(double v) {
        super(v);
    }
}
