package com.poorknight.recipebook;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class RecipeBook {

	private UserId userId;
	private List<RecipeId> recipeIds;

	public RecipeBook(UserId userId, List<RecipeId> recipeIds) {
		this.userId = userId;
		this.recipeIds = recipeIds;
	}

	public UserId getUserId() {
		return userId;
	}

	public List<RecipeId> getRecipeIds() {
		return Collections.unmodifiableList(recipeIds);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RecipeBook that = (RecipeBook) o;
		return Objects.equal(userId, that.userId) && Objects.equal(recipeIds, that.recipeIds);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(userId, recipeIds);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("userId", userId).add("recipeIds", recipeIds).toString();
	}

	public static class RecipeId {

		private String value;

		public RecipeId(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RecipeId recipeId = (RecipeId) o;
			return Objects.equal(value, recipeId.value);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("value", value).toString();
		}
	}

	public static class UserId {

		private String value;

		public UserId(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			UserId userId = (UserId) o;
			return Objects.equal(value, userId.value);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this).add("value", value).toString();
		}
	}
}
