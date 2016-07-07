package com.poorknight.recipe;

@SuppressWarnings("WeakerAccess")
public class Recipe {

	private final RecipeId id;
	private final String name;
	private final String content;
	private final UserId owningUserId;

	public Recipe(final String name, final String content, final UserId owningUserId) {
		this(null, name, content, owningUserId);
	}

	public Recipe(final RecipeId id, final String name, final String content, final UserId owningUserId) {
		this.id = id;
		this.name = name;
		this.content = content;
		this.owningUserId = owningUserId;
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((owningUserId == null) ? 0 : owningUserId.hashCode());
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
		final Recipe other = (Recipe) obj;
		if (content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!content.equals(other.content)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (owningUserId == null) {
			if (other.owningUserId != null) {
				return false;
			}
		} else if (!owningUserId.equals(other.owningUserId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Recipe [id=" + id + ", name=" + name + ", content=" + content + ", userId=" + owningUserId + "]";
	}
}
