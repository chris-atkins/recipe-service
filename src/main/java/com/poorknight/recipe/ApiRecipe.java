package com.poorknight.recipe;

public class ApiRecipe {
	private String recipeId;
	private String recipeName;
	private String recipeContent;
	private Boolean editable;

	public ApiRecipe() {
		this(null, null, null, null);
	}

	public ApiRecipe(final String recipeName, final String recipeContent, final Boolean editable) {
		this(null, recipeName, recipeContent, editable);
	}

	public ApiRecipe(final String recipeId, final String recipeName, final String recipeCcontent, final Boolean editable) {
		super();
		this.recipeId = recipeId;
		this.recipeName = recipeName;
		this.recipeContent = recipeCcontent;
		this.editable = editable;
	}

	public String getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(final String recipeId) {
		this.recipeId = recipeId;
	}

	public String getRecipeName() {
		return recipeName;
	}

	public void setRecipeName(final String recipeName) {
		this.recipeName = recipeName;
	}

	public String getRecipeContent() {
		return recipeContent;
	}

	public void setRecipeContent(final String recipeContent) {
		this.recipeContent = recipeContent;
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(final Boolean editable) {
		this.editable = editable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((editable == null) ? 0 : editable.hashCode());
		result = prime * result + ((recipeContent == null) ? 0 : recipeContent.hashCode());
		result = prime * result + ((recipeId == null) ? 0 : recipeId.hashCode());
		result = prime * result + ((recipeName == null) ? 0 : recipeName.hashCode());
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
		final ApiRecipe other = (ApiRecipe) obj;
		if (editable == null) {
			if (other.editable != null) {
				return false;
			}
		} else if (!editable.equals(other.editable)) {
			return false;
		}
		if (recipeContent == null) {
			if (other.recipeContent != null) {
				return false;
			}
		} else if (!recipeContent.equals(other.recipeContent)) {
			return false;
		}
		if (recipeId == null) {
			if (other.recipeId != null) {
				return false;
			}
		} else if (!recipeId.equals(other.recipeId)) {
			return false;
		}
		if (recipeName == null) {
			if (other.recipeName != null) {
				return false;
			}
		} else if (!recipeName.equals(other.recipeName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "ApiRecipe [recipeId=" + recipeId + ", recipeName=" + recipeName + ", recipeContent=" + recipeContent + ", editable=" + editable + "]";
	}

}
