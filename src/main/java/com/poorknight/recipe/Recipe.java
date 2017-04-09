package com.poorknight.recipe;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class Recipe {

	private final RecipeId id;
	private final String name;
	private final String content;
	private final UserId owningUserId;
	private final RecipeImage image;

	public Recipe(final String name, final String content, final UserId owningUserId) {
		this(null, name, content, owningUserId, null);
	}

	public Recipe(final String name, final String content, final UserId owningUserId, RecipeImage image) {
		this(null, name, content, owningUserId, image);
	}

	public Recipe(final RecipeId id, final String name, final String content, final UserId owningUserId) {
		this(id, name, content, owningUserId, null);
	}

	public Recipe(final RecipeId id, final String name, final String content, final UserId owningUserId, RecipeImage image) {
		this.id = id;
		this.name = name;
		this.content = content;
		this.owningUserId = owningUserId;
		this.image = image;
	}

	public RecipeId getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}

	public UserId getOwningUserId() {
		return this.owningUserId;
	}

	public RecipeImage getImage() {
		return this.image;
	}

	public static class RecipeId {

		private final String value;

		public RecipeId(final String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final RecipeId other = (RecipeId) obj;
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "RecipeId [value=" + value + "]";
		}
	}

	public static class UserId {

		private final String value;

		public UserId(final String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final UserId other = (UserId) obj;
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "UserId [value=" + value + "]";
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Recipe recipe = (Recipe) o;
		return Objects.equals(id, recipe.id) && Objects.equals(name, recipe.name) && Objects.equals(content,
				recipe.content) && Objects.equals(owningUserId, recipe.owningUserId) && Objects.equals(image,
				recipe.image);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, content, owningUserId, image);
	}

	@Override
	public String toString() {
		return com.google.common.base.Objects.toStringHelper(this).add("id", id).add("name", name).add("content",
				content).add("owningUserId", owningUserId).add("image", image).toString();
	}
}
