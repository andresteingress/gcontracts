package org.gcontracts.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Represents a <b>method's postcondition</b>. <p/>A method's postcondition is executed <i>after</i> a method call
 * has finished. A successor's postcondition strengthens the postcondition of its parent class, e.g. if A.someMethod
 * declares a postcondition and B.someMethod overrides the method the postconditions are combined with a boolean AND.
 *
 * @author andre.steingress@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Ensures {
    public abstract Class value();
}