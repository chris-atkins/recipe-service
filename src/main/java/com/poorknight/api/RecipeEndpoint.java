package com.poorknight.api;

import com.codahale.metrics.annotation.Timed;
import com.poorknight.recipe.*;
import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.Recipe.UserId;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.recipe.search.SearchTag;
import com.poorknight.recipebook.ApiRecipeId;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Path("/recipe")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RecipeEndpoint {

	private static final int TAG_SUGGESTION_LIMIT = 24;

	private final RecipeRepository recipeRepository;
	private final RecipeTranslator recipeTranslator;
	private final RecipeSearchStringParser recipeSearchStringParser;
	private RecipeBookEndpoint recipeBookEndpoint;
	private RecipeBookToRecipeTranslator recipeBookToRecipeTranslator;

	public RecipeEndpoint(final RecipeRepository recipeRepository,
						  final RecipeTranslator recipeTranslator,
						  final RecipeSearchStringParser recipeSearchStringParser,
						  final RecipeBookEndpoint recipeBookEndpoint,
						  final RecipeBookToRecipeTranslator recipeBookToRecipeTranslator) {

		this.recipeRepository = recipeRepository;
		this.recipeTranslator = recipeTranslator;
		this.recipeSearchStringParser = recipeSearchStringParser;
		this.recipeBookEndpoint = recipeBookEndpoint;
		this.recipeBookToRecipeTranslator = recipeBookToRecipeTranslator;
	}

	@GET
	@Timed(name = "getRecipes")
	@Path("/")
	public List<ApiRecipe> getRecipes(@QueryParam("searchString") final String searchString,
									  @QueryParam("recipeBook") final String recipeBookUserId,
									  @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);

		if(searchString != null && recipeBookUserId != null) {
			throw new RuntimeException("A query with both recipeBook and searchString is not currently supported");
		}

		if (searchString != null) {
			return searchRecipes(searchString, requestingUserId);
		}

		if(recipeBookUserId != null) {
			return findRecipesForRecipeBook(recipeBookUserId, requestingUserId);
		}
		return findAllRecipes(requestingUserId);
	}

	private List<ApiRecipe> searchRecipes(final String searchString, final UserId requestingUserId) {
		final List<SearchTag> searchTags = recipeSearchStringParser.parseSearchString(searchString);
		final List<Recipe> allRecipes = recipeRepository.searchRecipes(searchTags);
		return recipeTranslator.toApi(allRecipes, requestingUserId);
	}

	private List<ApiRecipe> findRecipesForRecipeBook(String recipeBookUserId, UserId requestingUserId) {
		final List<ApiRecipeId> recipeBook = recipeBookEndpoint.getRecipeBook(recipeBookUserId);
		final List<RecipeId> recipeIds = recipeBookToRecipeTranslator.translateIds(recipeBook);
		final List<Recipe> recipesById = recipeRepository.findRecipesWithIds(recipeIds);
		return recipeTranslator.toApi(recipesById, requestingUserId);
	}

	private List<ApiRecipe> findAllRecipes(final UserId requestingUserId) {
		final List<Recipe> recipes = recipeRepository.findAllRecipes();
		return recipeTranslator.toApi(recipes, requestingUserId);
	}

	@GET
	@Timed(name = "getRecipe")
	@Path("/{id}")
	public ApiRecipe getRecipe(@PathParam("id") final String recipeId, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);
		final Recipe recipe = recipeRepository.findRecipeById(id);

		if (recipe == null) {
			throw new NotFoundException("No recipe found with id: " + recipeId);
		}

		return enrich(recipe, requestingUserId);
	}

	private ApiRecipe enrich(final Recipe recipe, final UserId requestingUserId) {
		final ApiRecipe apiRecipe = recipeTranslator.toApi(recipe, requestingUserId);
		apiRecipe.setOwnTags(requestingUserId == null
				? Collections.emptyList()
				: recipeRepository.findTagsAddedByUser(recipe.getId(), requestingUserId));
		return apiRecipe;
	}

	@GET
	@Timed(name = "getTagSuggestions")
	@Path("/tag-suggestions")
	public List<String> getTagSuggestions(@QueryParam("category") final String category) {
		if (StringUtils.isEmpty(category)) {
			return Collections.emptyList();
		}
		return recipeRepository.suggestTagsForCategory(category, TAG_SUGGESTION_LIMIT);
	}

	@POST
	@Timed(name = "postRecipe")
	@Path("/")
	public ApiRecipe postRecipe(final ApiRecipe recipe, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);
		if (requestingUserId == null) {
			throw new WebApplicationException("A user id must be in the header to perform this operation.", 401);
		}

		final Recipe recipeToSave = recipeTranslator.toDomain(recipe, requestingUserId);
		final Recipe savedRecipe = recipeRepository.saveNewRecipe(recipeToSave);
		return recipeTranslator.toApi(savedRecipe, requestingUserId);
	}

	@PUT
	@Timed(name = "putRecipe")
	@Path("/{id}")
	public ApiRecipe putRecipe(final ApiRecipe recipe, @PathParam("id") final String recipeId, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		validateUserOwnsRecipe(recipeId, requestingUserIdString, "update");

		try {
			return putRecipeThrowingExceptions(recipe, requestingUserIdString);

		} catch (NoRecipeExistsForIdException e) {
			throw new NotFoundException(e);
		}
	}

	private void validateUserOwnsRecipe(final String recipeId, final String requestingUserIdString, final String action) {
		if (StringUtils.isEmpty(requestingUserIdString)) {
			throw new WebApplicationException("A user id must be in the header to perform this operation.", 401);
		}
		final Recipe existingRecipe = recipeRepository.findRecipeById(recipeTranslator.recipeIdFor(recipeId));
		if (existingRecipe != null && !requestingUserIdString.equals(existingRecipe.getOwningUserId().getValue())) {
			throw new WebApplicationException("Only the original creator of a recipe may " + action + " it.", 401);
		}
	}

	private ApiRecipe putRecipeThrowingExceptions(ApiRecipe recipe, @HeaderParam("RequestingUser") String requestingUserIdString) {
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);
		final Recipe recipeToSave = recipeTranslator.toDomain(recipe, requestingUserId);
		final Recipe savedRecipe = recipeRepository.updateRecipe(recipeToSave);
		return recipeTranslator.toApi(savedRecipe, requestingUserId);
	}

	@PUT
	@Timed(name = "rateRecipe")
	@Path("/{id}/rating")
	public ApiRecipe rateRecipe(@PathParam("id") final String recipeId, final ApiRatingRequest ratingRequest, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);
		if (requestingUserId == null) {
			throw new WebApplicationException("A user id must be in the header to perform this operation.", 401);
		}

		final int value = (ratingRequest == null) ? 0 : ratingRequest.getValue();
		if (value < 1 || value > 5) {
			throw new WebApplicationException("A rating must be between 1 and 5.", 400);
		}

		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		try {
			final Recipe ratedRecipe = recipeRepository.rateRecipe(id, requestingUserId, value);
			return recipeTranslator.toApi(ratedRecipe, requestingUserId);
		} catch (NoRecipeExistsForIdException e) {
			throw new NotFoundException(e);
		}
	}

	@POST
	@Timed(name = "postTag")
	@Path("/{id}/tag")
	public ApiRecipe postTag(@PathParam("id") final String recipeId, final ApiTagRequest tagRequest, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);
		if (requestingUserId == null) {
			throw new WebApplicationException("A user id must be in the header to perform this operation.", 401);
		}

		final String tag = (tagRequest == null) ? null : StringUtils.trimToNull(tagRequest.getTag());
		if (StringUtils.isBlank(tag)) {
			throw new WebApplicationException("A tag must not be blank.", 400);
		}

		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		try {
			final Recipe addedRecipe = recipeRepository.addTag(id, tag, requestingUserId);
			return enrich(addedRecipe, requestingUserId);
		} catch (NoRecipeExistsForIdException e) {
			throw new NotFoundException(e);
		}
	}

	@DELETE
	@Timed(name = "deleteTag")
	@Path("/{id}/tag")
	public ApiRecipe deleteTag(@PathParam("id") final String recipeId, @QueryParam("tag") final String tag, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		final UserId requestingUserId = recipeTranslator.userIdFor(requestingUserIdString);
		if (requestingUserId == null) {
			throw new WebApplicationException("A user id must be in the header to perform this operation.", 401);
		}

		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		final boolean removed = recipeRepository.removeTag(id, tag, requestingUserId);
		if (!removed) {
			throw new WebApplicationException("You can only remove a tag you added.", 403);
		}
		return enrich(recipeRepository.findRecipeById(id), requestingUserId);
	}

	@DELETE
	@Timed(name = "deleteRecipe")
	@Path("/{id}")
	public void deleteRecipe(@PathParam("id") final String recipeId, @HeaderParam("RequestingUser") final String requestingUserIdString) {
		validateUserOwnsRecipe(recipeId, requestingUserIdString, "delete");
		final RecipeId id = recipeTranslator.recipeIdFor(recipeId);
		recipeRepository.deleteRecipe(id);
	}
}