package org.gcontracts.annotations.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be used to declare custom precondition annotations for reusing
 * common preconditions.
 *
 * @see org.gcontracts.annotations.common.NotNull
 *
 * @author andre.steingress@gmail.com
 */
@ContractElement
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Precondition {
}
