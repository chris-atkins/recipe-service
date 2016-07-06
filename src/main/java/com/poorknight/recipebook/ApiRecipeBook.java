package com.poorknight.recipebook;

import com.google.common.base.Objects;

import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

public class ApiRecipeBook {

	private String userId;
	private List<ApiRecipeId> recipeIds;

	public ApiRecipeBook() {
		//empty constructor on purpose
	}

	public ApiRecipeBook(String userId, List<ApiRecipeId> recipeIds) {
		this.userId = userId;
		this.recipeIds = recipeIds;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<ApiRecipeId> getRecipeIds() {
		return recipeIds;
	}

	public void setRecipeIds(List<ApiRecipeId> recipeIds) {
		this.recipeIds = recipeIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ApiRecipeBook that = (ApiRecipeBook) o;
		return Objects.equal(userId, that.userId) && Objects.equal(recipeIds, that.recipeIds);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(userId, recipeIds);
	}

	@Override
	public String toString() {
		return toStringHelper(this).add("userId", userId).add("recipeIds", recipeIds).toString();
	}

	public static class ApiRecipeId {

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
}
