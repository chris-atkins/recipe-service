package com.poorknight.api;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipebook.ApiRecipeId;
import com.poorknight.recipebook.RecipeBook;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import com.poorknight.recipebook.RecipeBookRepository;
import com.poorknight.recipebook.RecipeBookTranslator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@Path("/user/{userId}/recipe-book")
public class RecipeBookEndpoint {

	private RecipeBookRepository recipeBookRepository;
	private RecipeBookTranslator recipeBookTranslator;

	public RecipeBookEndpoint(RecipeBookRepository recipeBookRepository, RecipeBookTranslator recipeBookTranslator) {
		this.recipeBookRepository = recipeBookRepository;
		this.recipeBookTranslator = recipeBookTranslator;
	}

	@POST
    @Path("/")
    @Timed(name = "postIdToRecipeBook")
    public ApiRecipeId postIdToRecipeBook(@PathParam("userId") final String userIdString, final ApiRecipeId apiRecipeId) {
		UserId userId = recipeBookTranslator.userIdFor(userIdString);
		RecipeId recipeId = recipeBookTranslator.toDomain(apiRecipeId);

		final RecipeId resultsFromAddingRecipeId = recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId);
		return recipeBookTranslator.recipeIdFor(resultsFromAddingRecipeId);
	}

	@GET
	@Path("/")
	@Timed(name = "getRecipeBook")
	public List<ApiRecipeId> getRecipeBook(@PathParam("userId") final String userIdString) {
		UserId userId = recipeBookTranslator.userIdFor(userIdString);
		final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
		return recipeBookTranslator.toApi(recipeBook);
	}
}
