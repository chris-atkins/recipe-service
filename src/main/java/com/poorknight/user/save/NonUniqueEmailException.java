package com.poorknight.user.save;

public class NonUniqueEmailException extends RuntimeException {

	private static final long serialVersionUID = -4095267555424517810L;

	public NonUniqueEmailException(final String email) {
		super("Attempting to save a new user with an email that already exists: " + email);
	}
}
