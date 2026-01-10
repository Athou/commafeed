package com.commafeed.security.password;

import java.util.List;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

import lombok.Setter;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

	@Setter
	private static int minimumPasswordLength;

	@Override
	public void initialize(ValidPassword constraintAnnotation) {
		// nothing to do
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}

		PasswordValidator validator = buildPasswordValidator();
		RuleResult result = validator.validate(new PasswordData(value));

		if (result.isValid()) {
			return true;
		}

		List<String> messages = validator.getMessages(result);
		String message = String.join(System.lineSeparator(), messages);
		context.buildConstraintViolationWithTemplate(message).addConstraintViolation().disableDefaultConstraintViolation();
		return false;
	}

	private PasswordValidator buildPasswordValidator() {
		return new PasswordValidator(
				// length
				new LengthRule(minimumPasswordLength, 256),
				// no whitespace
				new WhitespaceRule());
	}
}
