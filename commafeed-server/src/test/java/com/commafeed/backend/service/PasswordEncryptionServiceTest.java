package com.commafeed.backend.service;

import java.util.HexFormat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordEncryptionServiceTest {

	@Test
	void authenticate() {
		String password = "password";
		byte[] salt = "abcdefgh".getBytes();

		PasswordEncryptionService passwordEncryptionService = new PasswordEncryptionService();
		byte[] encryptedPassword = passwordEncryptionService.getEncryptedPassword(password, salt);

		// make sure the encrypted password is always the same for a fixed salt
		Assertions.assertEquals("8b4660158141d9f4f7865718b9a2b940a3e3cea9", HexFormat.of().formatHex(encryptedPassword));
		Assertions.assertTrue(passwordEncryptionService.authenticate(password, encryptedPassword, salt));
	}

}