package com.poorknight.recipe.exception;

import com.poorknight.recipe.Recipe;

public class NoRecipeExistsForIdException extends RuntimeException {
    public NoRecipeExistsForIdException(Recipe.RecipeId id) {
        super("Cannot update recipe - no recipe found with id: " + id.getValue());
    }
}