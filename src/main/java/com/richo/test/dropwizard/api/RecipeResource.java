package com.richo.test.dropwizard.api;

import java.util.List;

import com.poorknight.domain.Recipe;
import com.poorknight.persistence.RecipeRepository;

public class RecipeResource implements RecipeApi {

	private final RecipeRepository recipeRepository;

	public RecipeResource(final RecipeRepository recipeRepository) {
		this.recipeRepository = recipeRepository;
	}

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
