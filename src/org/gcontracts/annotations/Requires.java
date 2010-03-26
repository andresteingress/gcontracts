package org.gcontracts.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Represents a <b>method's precondition</b>.<p/>A method's precondition is executed <i>before</i> a method call. A
 * successor's precondition weakens the precondition of its parent class, e.g. if A.someMethod
 * declares a precondition and B.someMethod overrides the method the preconditions are combined with a boolean OR.
 *
 * @author andre.steingress@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Requires {
    Class value();
}