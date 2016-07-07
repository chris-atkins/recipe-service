package com.poorknight.recipe;

import com.poorknight.recipebook.ApiRecipeId;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecipeBookToRecipeTranslator {
	public List<Recipe.RecipeId> translateIds(List<ApiRecipeId> recipeIdsFromRecipeBook) {
		if(recipeIdsFromRecipeBook == null) {
			return Collections.emptyList();
		}
		return translateList(recipeIdsFromRecipeBook);
	}

	private List<Recipe.RecipeId> translateList(List<ApiRecipeId> recipeIdsFromRecipeBook) {
		List<Recipe.RecipeId> recipeIds = new ArrayList<>(recipeIdsFromRecipeBook.size());
		for(ApiRecipeId id : recipeIdsFromRecipeBook) {
			addIdToListIfValid(id, recipeIds);
		}
		return recipeIds;
	}

	private void addIdToListIfValid(ApiRecipeId id, List<Recipe.RecipeId> recipeIds) {
		if(isNotValid(id)) {
			return;
		}
		Recipe.RecipeId recipeId = new Recipe.RecipeId(id.getRecipeId());
		recipeIds.add(recipeId);
	}

	private boolean isNotValid(ApiRecipeId id) {
		if (id == null || StringUtils.isEmpty(id.getRecipeId())) {
			return true;
		}
		return false;
	}
}
