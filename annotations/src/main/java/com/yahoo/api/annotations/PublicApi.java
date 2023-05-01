// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For annotating packages that should be part of the public API.
 *
 * Must be placed in a file called package-info.java in the
 * package that is to be public.
 * @author Tony Vaagenes
 * @author gjoranv
 * @since 5.1.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PACKAGE)
@Documented
public @interface PublicApi {
}
