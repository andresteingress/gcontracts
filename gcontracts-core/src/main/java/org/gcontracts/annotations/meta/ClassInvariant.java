package org.gcontracts.annotations.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be used to declare custom class invariant annotations for reusing
 * common class invariants.
 *
 * @see org.gcontracts.annotations.common.NotNull
 *
 * @author andre.steingress@gmail.com
 */
@ContractElement
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassInvariant {
}
