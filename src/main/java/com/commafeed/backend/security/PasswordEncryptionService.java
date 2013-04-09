package com.commafeed.backend.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ejb.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.UserService;

// http://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
@Singleton
public class PasswordEncryptionService {

	private static final Logger log = LoggerFactory
			.getLogger(UserService.class);

	public boolean authenticate(String attemptedPassword,
			byte[] encryptedPassword, byte[] salt) {
		// Encrypt the clear-text password using the same salt that was used to
		// encrypt the original password
		byte[] encryptedAttemptedPassword = null;
		try {
			encryptedAttemptedPassword = getEncryptedPassword(
					attemptedPassword, salt);
		} catch (Exception e) {
			// should never happen
			log.error(e.getMessage(), e);
		}

		// Authentication succeeds if encrypted password that the user entered
		// is equal to the stored hash
		return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
	}

	public byte[] getEncryptedPassword(String password, byte[] salt) {
		// PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
		// specifically names SHA-1 as an acceptable hashing algorithm for
		// PBKDF2
		String algorithm = "PBKDF2WithHmacSHA1";
		// SHA-1 generates 160 bit hashes, so that's what makes sense here
		int derivedKeyLength = 160;
		// Pick an iteration count that works for you. The NIST recommends at
		// least 1,000 iterations:
		// http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
		// iOS 4.x reportedly uses 10,000:
		// http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
		int iterations = 20000;

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations,
				derivedKeyLength);

		byte[] bytes = null;
		try {
			SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);
			SecretKey key = f.generateSecret(spec);
			bytes = key.getEncoded();
		} catch (Exception e) {
			// should never happen
			log.error(e.getMessage(), e);
		}
		return bytes;
	}

	public byte[] generateSalt() {
		// VERY important to use SecureRandom instead of just Random

		byte[] salt = null;
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

			// Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
			salt = new byte[8];
			random.nextBytes(salt);
		} catch (NoSuchAlgorithmException e) {
			// should never happen
			log.error(e.getMessage(), e);
		}
		return salt;
	}

}
