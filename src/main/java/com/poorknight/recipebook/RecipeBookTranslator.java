package com.poorknight.recipebook;

import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class RecipeBookTranslator {

	public RecipeId toDomain(ApiRecipeId apiRecipeId) {
		if(apiRecipeId == null || isEmpty(apiRecipeId.getRecipeId())) {
			return null;
		}
		return new RecipeId(apiRecipeId.getRecipeId());
	}

	public UserId userIdFor(String userIdString) {
		if (isEmpty(userIdString)) {
			return null;
		}
		return new UserId(userIdString);
	}

	public ApiRecipeId recipeIdFor(RecipeId recipeId) {
		if(recipeId == null || isEmpty(recipeId.getValue())) {
			return null;
		}
		return new ApiRecipeId(recipeId.getValue());
	}

	public List<ApiRecipeId> toApi(RecipeBook recipeBook) {
		if(recipeBook == null) {
			return Collections.emptyList();
		}

		List<ApiRecipeId> apiRecipeIds = new ArrayList<>(recipeBook.getRecipeIds().size());
		for(RecipeId id : recipeBook.getRecipeIds()) {
			apiRecipeIds.add(recipeIdFor(id));
		}
		return apiRecipeIds;
	}
}
