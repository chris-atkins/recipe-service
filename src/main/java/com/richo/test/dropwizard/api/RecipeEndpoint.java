package com.richo.test.dropwizard.api;

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

import com.poorknight.api.ApiRecipe;
import com.poorknight.domain.Recipe;
import com.poorknight.domain.identities.RecipeId;
import com.poorknight.persistence.RecipeRepository;

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
	@Path("/")
	public ApiRecipe postRecipe(final ApiRecipe recipe) {
		final Recipe translatedRecipe = recipeTranslator.toDomain(recipe);
		final Recipe recipeToSave = new Recipe(translatedRecipe.getId(), translatedRecipe.getName(), htmlTransformer.translate(translatedRecipe.getContent()));
		final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipeToSave);
		return recipeTranslator.toApi(savedRecipe);
	}

	@GET
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
	@Path("/{id}")
	public void deleteRecipe(@PathParam("id") final String recipeId) {
		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		recipeRepository.deleteRecipe(id);
	}
}
