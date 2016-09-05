package com.poorknight.application.init;

import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.RecipeRepository;

import java.util.List;

public class OneTimeRecipeHtmlifier {

	private final RecipeRepository recipeRepository;

	public OneTimeRecipeHtmlifier(final RecipeRepository recipeRepository) {
		this.recipeRepository = recipeRepository;
	}

	public void transformAllRecipes() {
		final List<Recipe> allRecipes = recipeRepository.findAllRecipes();
		for(final Recipe recipe : allRecipes) {
			final Recipe alteredRecipe = buildAlteredRecipe(recipe);
			recipeRepository.updateRecipe(alteredRecipe);
		}
	}
	private Recipe buildAlteredRecipe(final Recipe recipe) {
		final String originalContent = recipe.getContent();

		final String alteredContent = addHtmlBreaksAndSurroundByDiv(originalContent);
		return new Recipe(recipe.getId(), recipe.getName(), alteredContent, recipe.getOwningUserId());
	}

	private String addHtmlBreaksAndSurroundByDiv(final String originalContent) {
		final String contentWithBrs = originalContent.replaceAll("\\n", "<br>");
		final String firstSpacesPass = contentWithBrs.replaceAll("  ", "&nbsp; ");
		final String secondSpacesPass = firstSpacesPass.replaceAll("  ", " &nbsp;");
		final String thirdSpacesPass = secondSpacesPass.replaceAll(" <br>", "&nbsp;<br>");
		return "<div><!--block-->" + thirdSpacesPass + "</div>";
	}
}
