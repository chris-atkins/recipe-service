package com.poorknight.recipebook;

import com.google.common.base.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;

@SuppressWarnings("unused")
public class ApiRecipeId {

	private String recipeId;

	public ApiRecipeId() {
		//empty constructor on purpose
	}

	public ApiRecipeId(String recipeId) {
		this.recipeId = recipeId;
	}

	public String getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(final String recipeId) {
		this.recipeId = recipeId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ApiRecipeId that = (ApiRecipeId) o;
		return Objects.equal(recipeId, that.recipeId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(recipeId);
	}

	@Override
	public String toString() {
		return toStringHelper(this).add("recipeId", recipeId).toString();
	}
}
