package org.gcontracts.annotations.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be used as meta element for contract elements, that is class-invariants,
 * pre- and post-conditions.<p/>
 *
 * <b>This meta-annotation is intented for internal use by GContracts.</b>
 *
 * @see org.gcontracts.annotations.common.NotNull
 *
 * @author andre.steingress@gmail.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface ContractElement {
}
