package com.poorknight.application.init.historical.run20160905;

import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.RecipeRepositoryInterface;

import java.util.List;

public class OneTimeRecipeHtmlifier {

	private final RecipeRepositoryInterface recipeRepository;

	public OneTimeRecipeHtmlifier(final RecipeRepositoryInterface recipeRepository) {
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
