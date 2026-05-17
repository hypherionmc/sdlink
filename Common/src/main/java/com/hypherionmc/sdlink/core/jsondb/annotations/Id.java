package com.hypherionmc.sdlink.core.jsondb.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * @author HypherionSA
 * Marker annotation to indicate which field is the ID field
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {FIELD, METHOD, ANNOTATION_TYPE})
public @interface Id {}