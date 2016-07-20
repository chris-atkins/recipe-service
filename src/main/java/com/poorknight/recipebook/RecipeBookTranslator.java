package com.poorknight.recipebook;

import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class RecipeBookTranslator {

	public RecipeId toDomain(final ApiRecipeId apiRecipeId) {
		if(apiRecipeId == null || isEmpty(apiRecipeId.getRecipeId())) {
			return null;
		}
		return new RecipeId(apiRecipeId.getRecipeId());
	}

	public UserId userIdFor(final String userIdString) {
		if (isEmpty(userIdString)) {
			return null;
		}
		return new UserId(userIdString);
	}

	public ApiRecipeId apiRecipeIdFor(final RecipeId recipeId) {
		if(recipeId == null || isEmpty(recipeId.getValue())) {
			return null;
		}
		return new ApiRecipeId(recipeId.getValue());
	}

	public List<ApiRecipeId> toApi(final RecipeBook recipeBook) {
		if(recipeBook == null) {
			return Collections.emptyList();
		}

		final List<ApiRecipeId> apiRecipeIds = new ArrayList<>(recipeBook.getRecipeIds().size());
		for(final RecipeId id : recipeBook.getRecipeIds()) {
			apiRecipeIds.add(apiRecipeIdFor(id));
		}
		return apiRecipeIds;
	}

	public RecipeId recipeIdFor(final String recipeIdString) {
		return new RecipeId(recipeIdString);
	}
}
