package com.poorknight.recipebook;

public class RecipeBookNotFoundException extends RuntimeException {

	public RecipeBookNotFoundException(final RecipeBook.UserId userId) {
		super("No recipe book found for user with id: " + userId.getValue());
	}
}
