package com.commafeed.backend.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// taken from http://www.javacodegeeks.com/2012/05/secure-password-storage-donts-dos-and.html
@Slf4j
@RequiredArgsConstructor
@Singleton
public class PasswordEncryptionService {

	public boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt) {
		if (StringUtils.isBlank(attemptedPassword)) {
			return false;
		}
		// Encrypt the clear-text password using the same salt that was used to
		// encrypt the original password
		byte[] encryptedAttemptedPassword = null;
		try {
			encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);
		} catch (Exception e) {
			// should never happen
			log.error(e.getMessage(), e);
		}

		if (encryptedAttemptedPassword == null) {
			return false;
		}

		// Authentication succeeds if encrypted password that the user entered
		// is equal to the stored hash
		return MessageDigest.isEqual(encryptedPassword, encryptedAttemptedPassword);
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

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

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
