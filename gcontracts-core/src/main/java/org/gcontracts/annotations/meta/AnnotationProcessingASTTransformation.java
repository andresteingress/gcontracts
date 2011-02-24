package org.gcontracts.annotations.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A meta-annotation which is used to specify an implementation class of
 * {@link org.gcontracts.common.spi.AnnotationProcessingASTTransformation}.
 *
 * @see org.gcontracts.common.spi.AnnotationProcessingASTTransformation
 *
 * @author andre.steingress@gmail.com
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotationProcessingASTTransformation {
    Class<? extends org.gcontracts.common.spi.AnnotationProcessingASTTransformation>[] value();
}
