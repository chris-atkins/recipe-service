package com.richo.test.dropwizard.api;

import java.util.ArrayList;
import java.util.List;

import com.poorknight.api.ApiRecipe;
import com.poorknight.domain.Recipe;

public class RecipeTranslator {

	public ApiRecipe translate(final Recipe recipe) {
		return new ApiRecipe(recipe.getId().getValue(), recipe.getName(), recipe.getContent());
	}

	public List<ApiRecipe> translate(final List<Recipe> recipesFromRepository) {
		final List<ApiRecipe> results = new ArrayList<>(recipesFromRepository.size());
		for (final Recipe recipe : recipesFromRepository) {
			results.add(translate(recipe));
		}
		return results;
	}
}
