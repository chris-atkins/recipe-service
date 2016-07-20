package com.poorknight.api;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipebook.*;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Path("/user/{userId}/recipe-book")
public class RecipeBookEndpoint {

	private final RecipeBookRepository recipeBookRepository;
	private final RecipeBookTranslator recipeBookTranslator;

	public RecipeBookEndpoint(final RecipeBookRepository recipeBookRepository, final RecipeBookTranslator recipeBookTranslator) {
		this.recipeBookRepository = recipeBookRepository;
		this.recipeBookTranslator = recipeBookTranslator;
	}

	@POST
    @Path("/")
    @Timed(name = "postIdToRecipeBook")
    public ApiRecipeId postIdToRecipeBook(@PathParam("userId") final String userIdString, final ApiRecipeId apiRecipeId, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		validateUserIsAllowedToAlterBook(userIdString, requestingUserIdString);

		final UserId userId = recipeBookTranslator.userIdFor(userIdString);
		final RecipeId recipeId = recipeBookTranslator.toDomain(apiRecipeId);

		try {
			final RecipeId resultsFromAddingRecipeId = recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId);
			return recipeBookTranslator.apiRecipeIdFor(resultsFromAddingRecipeId);
		} catch (final InvalidIdException e) {
			throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
		}
	}

	private void validateUserIsAllowedToAlterBook(final String userIdString, final String requestingUserIdString) {
		if (!userIdString.equals(requestingUserIdString)) {
			throw new WebApplicationException("Invalid user. Only the owner of a recipe book may alter it.", 401);
		}
	}

	@GET
	@Path("/")
	@Timed(name = "getRecipeBook")
	public List<ApiRecipeId> getRecipeBook(@PathParam("userId") final String userIdString) {
		final UserId userId = recipeBookTranslator.userIdFor(userIdString);

		try {
			final RecipeBook recipeBook = recipeBookRepository.getRecipeBook(userId);
			return recipeBookTranslator.toApi(recipeBook);
		} catch (final InvalidIdException e) {
			return Collections.emptyList();
		}
	}

	@DELETE
	@Path("/{recipeId}")
	@Timed(name = "deleteRecipeFromBook")
	public void deleteRecipeFromBook(@PathParam("userId") final String userIdString, @PathParam("recipeId") final String recipeIdString, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		validateUserIsAllowedToAlterBook(userIdString, requestingUserIdString);

		final UserId userId = recipeBookTranslator.userIdFor(userIdString);
		final RecipeId recipeId = recipeBookTranslator.recipeIdFor(recipeIdString);

		try {
			recipeBookRepository.deleteRecipeFromRecipeBook(userId, recipeId);
		} catch(final RecipeNotInBookException e) {
			throw new NotFoundException(e);
		}
	}
}
