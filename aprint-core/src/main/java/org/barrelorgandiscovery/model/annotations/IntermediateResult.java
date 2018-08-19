package org.barrelorgandiscovery.model.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark objects as intermediate result (should not appear in
 * model)
 * 
 * @author pfreydiere
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface IntermediateResult {

}
