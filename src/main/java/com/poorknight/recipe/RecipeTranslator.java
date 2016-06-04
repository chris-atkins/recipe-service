package com.poorknight.recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeTranslator {

	public ApiRecipe toApi(final Recipe recipe) {
		return new ApiRecipe(recipe.getId().getValue(), recipe.getName(), recipe.getContent());
	}

	public List<ApiRecipe> toApi(final List<Recipe> recipesFromRepository) {
		final List<ApiRecipe> results = new ArrayList<>(recipesFromRepository.size());
		for (final Recipe recipe : recipesFromRepository) {
			results.add(toApi(recipe));
		}
		return results;
	}

	public Recipe toDomain(final ApiRecipe apiRecipe) {
		final RecipeId recipeId = recipeIdFor(apiRecipe.getRecipeId());
		return new Recipe(recipeId, apiRecipe.getRecipeName(), apiRecipe.getRecipeContent());
	}

	public RecipeId recipeIdFor(final String id) {
		return id == null ? null : new RecipeId(id);
	}
}
