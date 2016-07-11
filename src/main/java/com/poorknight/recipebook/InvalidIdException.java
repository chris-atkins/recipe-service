package com.poorknight.recipebook;

public class InvalidIdException extends RuntimeException {

	public InvalidIdException(String idAsString) {
		super("The passed ID is not valid: " + idAsString);
	}
}