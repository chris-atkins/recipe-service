package com.poorknight.recipebook;

public class RecipeNotInBookException extends RuntimeException {

	public RecipeNotInBookException(RecipeBook.RecipeId recipeId) {
		super("Recipe does not exist in recipe book: " + recipeId.getValue());
	}
}