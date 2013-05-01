package com.commafeed.frontend;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import com.commafeed.backend.model.UserRole.Role;

@Inherited
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityCheck {

	/**
	 * Roles needed.
	 */
	@Nonbinding
	Role value() default Role.USER;

	@Nonbinding
	boolean apiKeyAllowed() default false;
}