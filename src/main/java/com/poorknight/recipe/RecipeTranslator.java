package com.poorknight.recipe;

import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.Recipe.UserId;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class RecipeTranslator {

	public ApiRecipe toApi(final Recipe recipe, final UserId requestingUserId) {
		final boolean isEditable = determineIsEditable(recipe.getOwningUserId(), requestingUserId);
		final ApiRecipe apiRecipe = new ApiRecipe(recipe.getId().getValue(), recipe.getName(), recipe.getContent(), isEditable, recipe.getImage());
		apiRecipe.setCategory(recipe.getCategory());
		apiRecipe.setTags(recipe.getTags());
		return apiRecipe;
	}

	private boolean determineIsEditable(final UserId owningUserId, final UserId requestingUserId) {
		if (owningUserId == null || requestingUserId == null) {
			return false;
		}
		return requestingUserId.equals(owningUserId);
	}

	public List<ApiRecipe> toApi(final List<Recipe> recipesFromRepository, final UserId requestingUserId) {
		final List<ApiRecipe> results = new ArrayList<>(recipesFromRepository.size());
		for (final Recipe recipe : recipesFromRepository) {
			results.add(toApi(recipe, requestingUserId));
		}
		return results;
	}

	public Recipe toDomain(final ApiRecipe apiRecipe, final UserId userId) {
		final RecipeId recipeId = recipeIdFor(apiRecipe.getRecipeId());
		return new Recipe(recipeId, apiRecipe.getRecipeName(), apiRecipe.getRecipeContent(), userId, apiRecipe.getImage(), apiRecipe.getCategory(), apiRecipe.getTags());
	}

	public RecipeId recipeIdFor(final String id) {
		return id == null ? null : new RecipeId(id);
	}

	public UserId userIdFor(final String id) {
		return StringUtils.isEmpty(id) ? null : new UserId(id);
	}
}
