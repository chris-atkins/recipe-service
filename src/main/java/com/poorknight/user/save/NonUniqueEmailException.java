package com.poorknight.user.save;

public class NonUniqueEmailException extends RuntimeException {

	public NonUniqueEmailException(final String email) {
		super("Attempting to save a new user with an email that already exists: " + email);
	}
}
