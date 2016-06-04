package com.poorknight.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipe.ApiRecipe;
import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.RecipeId;
import com.poorknight.recipe.RecipeRepository;
import com.poorknight.recipe.RecipeTranslator;
import com.poorknight.recipe.save.TextToHtmlTranformer;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.recipe.search.SearchTag;

@Path("/recipe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeEndpoint {

	private final RecipeRepository recipeRepository;
	private final RecipeTranslator recipeTranslator;
	private final RecipeSearchStringParser recipeSearchStringParser;
	private final TextToHtmlTranformer htmlTransformer;

	public RecipeEndpoint(final RecipeRepository recipeRepository, //
			final RecipeTranslator recipeTranslator, //
			final RecipeSearchStringParser recipeSearchStringParser, //
			final TextToHtmlTranformer htmlTransformer) {

		this.recipeRepository = recipeRepository;
		this.recipeTranslator = recipeTranslator;
		this.recipeSearchStringParser = recipeSearchStringParser;
		this.htmlTransformer = htmlTransformer;
	}

	@GET
	@Timed(name = "getRecipes")
	@Path("/")
	public List<ApiRecipe> getRecipes(@QueryParam("searchString") final String searchString) {
		if (searchString == null) {
			return findAllRecipes();
		}
		return searchRecipes(searchString);
	}

	private List<ApiRecipe> findAllRecipes() {
		final List<Recipe> recipes = recipeRepository.findAllRecipes();
		return recipeTranslator.toApi(recipes);
	}

	private List<ApiRecipe> searchRecipes(final String searchString) {
		final List<SearchTag> searchTags = recipeSearchStringParser.parseSearchString(searchString);
		final List<Recipe> allRecipes = recipeRepository.searchRecipes(searchTags);
		return recipeTranslator.toApi(allRecipes);
	}

	@POST
	@Timed(name = "postRecipe")
	@Path("/")
	public ApiRecipe postRecipe(final ApiRecipe recipe) {
		final Recipe translatedRecipe = recipeTranslator.toDomain(recipe);
		final Recipe recipeToSave = new Recipe(translatedRecipe.getId(), translatedRecipe.getName(), htmlTransformer.translate(translatedRecipe.getContent()));
		final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipeToSave);
		return recipeTranslator.toApi(savedRecipe);
	}

	@GET
	@Timed(name = "getRecipe")
	@Path("/{id}")
	public ApiRecipe getRecipe(@PathParam("id") final String recipeId) {
		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		final Recipe recipe = recipeRepository.findRecipeById(id);

		if (recipe == null) {
			throw new NotFoundException("No recipe found with id: " + recipeId);
		}

		return recipeTranslator.toApi(recipe);
	}

	@DELETE
	@Timed(name = "deleteRecipe")
	@Path("/{id}")
	public void deleteRecipe(@PathParam("id") final String recipeId) {
		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		recipeRepository.deleteRecipe(id);
	}
}
