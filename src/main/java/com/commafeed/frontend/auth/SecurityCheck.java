package com.commafeed.frontend.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.commafeed.backend.model.UserRole.Role;

@Inherited
@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityCheck {

	/**
	 * Roles needed.
	 */
	Role value() default Role.USER;

	boolean apiKeyAllowed() default false;
}