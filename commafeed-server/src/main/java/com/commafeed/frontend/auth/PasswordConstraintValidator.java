package com.commafeed.frontend.auth;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;

public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

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
				new LengthRule(8, 128),
				// 1 uppercase char
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				// 1 lowercase char
				new CharacterRule(EnglishCharacterData.LowerCase, 1),
				// 1 digit
				new CharacterRule(EnglishCharacterData.Digit, 1),
				// 1 special char
				new CharacterRule(EnglishCharacterData.Special, 1),
				// no whitespace
				new WhitespaceRule());
	}

}
