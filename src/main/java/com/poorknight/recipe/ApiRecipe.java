package com.poorknight.recipe;

import java.util.Objects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ApiRecipe {
	private String recipeId;
	private String recipeName;
	private String recipeContent;
	private boolean editable;
	private RecipeImage image;

	public ApiRecipe() {
		this(null, null, null, false);
	}

	public ApiRecipe(final String recipeName, final String recipeContent, final boolean editable) {
		this(null, recipeName, recipeContent, editable);
	}

	public ApiRecipe(final String recipeId, final String recipeName, final String recipeContent, final boolean editable) {
		this(recipeId, recipeName, recipeContent, editable, null);
	}

	public ApiRecipe(final String recipeId, final String recipeName, final String recipeCcontent, final boolean editable, RecipeImage image) {
		super();
		this.recipeId = recipeId;
		this.recipeName = recipeName;
		this.recipeContent = recipeCcontent;
		this.editable = editable;
		this.image = image;
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

	public boolean getEditable() {
		return editable;
	}

	public void setEditable(final boolean editable) {
		this.editable = editable;
	}

	public RecipeImage getImage() {
		return image;
	}

	public void setImage(final RecipeImage image) {
		this.image = image;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final ApiRecipe apiRecipe = (ApiRecipe) o;
		return editable == apiRecipe.editable && Objects.equals(recipeId, apiRecipe.recipeId) && Objects.equals(
				recipeName,
				apiRecipe.recipeName) && Objects.equals(recipeContent, apiRecipe.recipeContent) && Objects.equals(image,
				apiRecipe.image);
	}

	@Override
	public int hashCode() {
		return Objects.hash(recipeId, recipeName, recipeContent, editable, image);
	}

	@Override
	public String toString() {
		return "ApiRecipe{" +
				"recipeId='" + recipeId + '\'' +
				", recipeName='" + recipeName + '\'' +
				", recipeContent='" + recipeContent + '\'' +
				", editable=" + editable +
				", image=" + image +
				'}';
	}
}
