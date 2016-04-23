package com.richo.test.dropwizard.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.poorknight.api.ApiRecipe;
import com.poorknight.domain.Recipe;
import com.poorknight.persistence.RecipeRepository;

@Path("/recipe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeEndpoint {

	private final RecipeRepository recipeRepository;
	private final RecipeTranslator recipeTranslator;
	private final RecipeSearchStringParser recipeSearchStringParser;

	public RecipeEndpoint(final RecipeRepository recipeRepository, final RecipeTranslator recipeTranslator, final RecipeSearchStringParser recipeSearchStringParser) {
		this.recipeRepository = recipeRepository;
		this.recipeTranslator = recipeTranslator;
		this.recipeSearchStringParser = recipeSearchStringParser;
	}

	@GET
	@Path("/")
	public List<ApiRecipe> getRecipes(final String searchString) {
		final List<SearchTag> searchTags = recipeSearchStringParser.parseSearchString(searchString);
		final List<Recipe> allRecipes = recipeRepository.searchRecipes(searchTags);
		return recipeTranslator.toApi(allRecipes);
	}

	@POST
	@Path("/")
	public ApiRecipe postRecipe(final ApiRecipe recipe) {
		final Recipe recipeToSave = recipeTranslator.toDomain(recipe);
		final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipeToSave);
		return recipeTranslator.toApi(savedRecipe);
	}

	@GET
	@Path("/{id}")
	public ApiRecipe getRecipe(final String recipeId) {
		throw new RuntimeException("implement me");
	}

	@DELETE
	@Path("/{id}")
	public void deleteRecipe(final String recipeId) {
		throw new RuntimeException("implement me");
	}
}
