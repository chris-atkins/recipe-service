package com.richo.test.dropwizard.api;

import java.util.List;

import com.poorknight.domain.Recipe;

public class RecipeResource implements RecipeApi {

	@Override
	public List<Recipe> getRecipes(final String searchString) {
		throw new RuntimeException("implement me");
	}

	@Override
	public Recipe postRecipe(final Recipe recipe) {
		throw new RuntimeException("implement me");
	}

	@Override
	public Recipe getRecipe(final Long recipeId) {
		throw new RuntimeException("implement me");
	}

	@Override
	public void deleteRecipe(final Long recipeId) {
		throw new RuntimeException("implement me");
	}

}
