package com.poorknight.api;

import com.poorknight.recipe.*;
import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.Recipe.UserId;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.recipe.search.SearchTag;
import com.poorknight.recipebook.ApiRecipeId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeEndpointTest {

	@InjectMocks
	private RecipeEndpoint endpoint;

	@Mock
	private RecipeRepository repository;

	@Mock
	private RecipeTranslator translator;

	@Mock
	private RecipeSearchStringParser recipeSearchStringParser;

	@Mock
	private RecipeBookEndpoint recipeBookEndpoint;

	@Mock
	private RecipeBookToRecipeTranslator recipeBookToRecipeTranslator;

	@Test
	public void getRecipes_WithNoSearchString_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final List<Recipe> recipesFromRepository = Collections.singletonList(new Recipe(null, null, null));
		final List<ApiRecipe> translatedRecipes = Collections.singletonList(new ApiRecipe());

		when(translator.userIdFor("user")).thenReturn(userId);
		when(repository.findAllRecipes()).thenReturn(recipesFromRepository);
		when(translator.toApi(recipesFromRepository, userId)).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes(null, null, "user");
		assertThat(results).isSameAs(translatedRecipes);
	}

	@Test
	public void getRecipes_WithSearchString_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final List<Recipe> recipesFromRepository = Collections.singletonList(new Recipe(null, null, null));
		final List<ApiRecipe> translatedRecipes = Collections.singletonList(new ApiRecipe());
		final List<SearchTag> searchTags = Collections.singletonList(new SearchTag(""));

		when(translator.userIdFor("user")).thenReturn(userId);
		when(recipeSearchStringParser.parseSearchString("search string")).thenReturn(searchTags);
		when(repository.searchRecipes(searchTags)).thenReturn(recipesFromRepository);
		when(translator.toApi(recipesFromRepository, new UserId("user"))).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes("search string", null, "user");
		assertThat(results).isSameAs(translatedRecipes);
	}

	@Test
	public void getRecipes_WithRecipeBook_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final List<ApiRecipeId> recipeIdsFromRecipeBook = Collections.singletonList(new ApiRecipeId("hi"));
		final List<RecipeId> recipeIdsToFind = Collections.singletonList(new RecipeId("hi"));
		final List<Recipe> recipesFromRepository = Collections.singletonList(new Recipe(null, null, null));
		final List<ApiRecipe> translatedRecipes = Collections.singletonList(new ApiRecipe());

		when(recipeBookEndpoint.getRecipeBook("recipeBookUserId")).thenReturn(recipeIdsFromRecipeBook);
		when(recipeBookToRecipeTranslator.translateIds(recipeIdsFromRecipeBook)).thenReturn(recipeIdsToFind);

		when(translator.userIdFor("user")).thenReturn(userId);
		when(repository.findRecipesWithIds(recipeIdsToFind)).thenReturn(recipesFromRepository);
		when(translator.toApi(recipesFromRepository, new UserId("user"))).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes(null, "recipeBookUserId", "user");
		assertThat(results).isSameAs(translatedRecipes);
	}

	@Test
	public void getRecipes_WithBothASearchStringAndARecipeBook_ThrowsException() throws Exception {
		try {
			endpoint.getRecipes("searchString", "recipeBookUserId", "user");
			fail("expectingException");
		} catch (RuntimeException e) {
			assertThat(e.getMessage()).isEqualTo("A query with both recipeBook and searchString is not currently supported");
		}
	}

	@Test
	public void postRecipe_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final ApiRecipe recipe = new ApiRecipe("name", "content", false);
		final Recipe translatedRecipe = new Recipe("hi", "content", new UserId("user"));
		final Recipe savedRecipe = new Recipe(new RecipeId("id"), "hi", "htmlified", new UserId("user"));
		final ApiRecipe translatedSavedRecipe = new ApiRecipe("id", "hi", "htmlified", false);

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.toDomain(recipe, new UserId("user"))).thenReturn(translatedRecipe);
		when(repository.saveNewRecipe(translatedRecipe)).thenReturn(savedRecipe);
		when(translator.toApi(savedRecipe, userId)).thenReturn(translatedSavedRecipe);

		final ApiRecipe results = endpoint.postRecipe(recipe, "user");
		assertThat(results).isEqualTo(translatedSavedRecipe);
	}

	@Test
	public void postRecipe_Throws401Exception_WhenNullUserIdIsSpecified() throws Exception {
		try {
			final ApiRecipe recipe = new ApiRecipe("id", "hi", "htmlified", false);
			endpoint.postRecipe(recipe, null);
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("A user id must be in the header to perform this operation.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void postRecipe_Throws401Exception_WhenAnEmptyUserIdIsSpecified() throws Exception {
		try {
			final ApiRecipe recipe = new ApiRecipe("id", "hi", "htmlified", false);
			endpoint.postRecipe(recipe, "");
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("A user id must be in the header to perform this operation.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void getRecipe_DelegatesToItsCollaborators() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		final UserId userId = new UserId("user");
		final Recipe domainRecipe = new Recipe(recipeId, "name", "content", new UserId("user"));
		final ApiRecipe apiRecipe = new ApiRecipe("id", "name", "content", false);

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(translator.userIdFor("user")).thenReturn(userId);
		when(repository.findRecipeById(recipeId)).thenReturn(domainRecipe);
		when(translator.toApi(domainRecipe, userId)).thenReturn(apiRecipe);

		final ApiRecipe results = endpoint.getRecipe("id", "user");
		assertThat(results).isEqualTo(apiRecipe);
	}

	@Test
	public void getRecipe_ThrowsNotFoundException_IfNoRecipeExists() throws Exception {
		final RecipeId recipeId = new RecipeId("id");

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(null);

		assertThrows(NotFoundException.class, () -> endpoint.getRecipe("id", "user"));
	}

	@Test
	public void deleteRecipe_DelegatesToItsCollaborators() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("user")));

		endpoint.deleteRecipe("id", "user");

		verify(repository).deleteRecipe(recipeId);
	}

	@Test
	public void deleteRecipe_Throws401Exception_WhenAnEmptyUserIdIsSpecified() throws Exception {
		try {
			endpoint.deleteRecipe("id", "");
			fail("expected exception");
		} catch (WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("A user id must be in the header to perform this operation.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void deleteRecipe_Throws401Exception_WhenANullUserIdIsSpecified() throws Exception {
		try {
			endpoint.deleteRecipe("id", null);
			fail("expected exception");
		} catch (WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("A user id must be in the header to perform this operation.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void deleteRecipe_Throws401Exception_WhenADifferentUserRequestsDeleteThanWhoCreatedIt() throws Exception {
		final RecipeId recipeId = new RecipeId("id");

		try {
			when(translator.recipeIdFor("id")).thenReturn(recipeId);
			when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("userId")));

			endpoint.deleteRecipe("id", "otherUserId");
			fail("expected exception");
		} catch (WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("Only the original creator of a recipe may delete it.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void putRecipe_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final RecipeId recipeId = new RecipeId("id");
		final ApiRecipe recipe = new ApiRecipe("id", "name", "content", false);
		final Recipe translatedRecipe = new Recipe(new RecipeId("id"), "hi", "content", new UserId("user"));
		final Recipe updatedRecipe = new Recipe(new RecipeId("id"), "hi", "htmlified", new UserId("user"));
		final ApiRecipe translatedUpdatedRecipe = new ApiRecipe("id", "hi", "htmlified", false);

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("user")));

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.toDomain(recipe, new UserId("user"))).thenReturn(translatedRecipe);
		when(repository.updateRecipe(translatedRecipe)).thenReturn(updatedRecipe);
		when(translator.toApi(updatedRecipe, userId)).thenReturn(translatedUpdatedRecipe);

		final ApiRecipe results = endpoint.putRecipe(recipe, "id", "user");
		assertThat(results).isEqualTo(translatedUpdatedRecipe);
	}

	@Test
	public void putRecipe_ThrowsNotFoundException_IfNoRecipeCanBeFoundToUpdate() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		final UserId userId = new UserId("user");
		final ApiRecipe recipe = new ApiRecipe("id", "name", "content", false);
		final Recipe translatedRecipe = new Recipe(new RecipeId("id"), "hi", "content", new UserId("user"));

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("user")));

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.toDomain(recipe, new UserId("user"))).thenReturn(translatedRecipe);
		final NoRecipeExistsForIdException repositoryException = new NoRecipeExistsForIdException(recipeId);
		when(repository.updateRecipe(translatedRecipe)).thenThrow(repositoryException);

		try {
			endpoint.putRecipe(recipe, "id", "user");
			fail("expected exception");
		} catch(NotFoundException e) {
			assertThat(e.getCause()).isEqualTo(repositoryException);
		}
	}

	@Test
	public void putRecipe_Throws401Exception_WhenAnEmptyUserIdIsSpecified() throws Exception {
		final ApiRecipe recipe = new ApiRecipe("id", "name", "content", false);

		try {
			endpoint.putRecipe(recipe, "id", "");
			fail("expected exception");
		} catch(WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("A user id must be in the header to perform this operation.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void putRecipe_Throws401Exception_WhenAnNullUserIdIsSpecified() throws Exception {
		final ApiRecipe recipe = new ApiRecipe("id", "name", "content", false);

		try {
			endpoint.putRecipe(recipe, "id", null);
			fail("expected exception");
		} catch(WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("A user id must be in the header to perform this operation.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void putRecipe_Throws401Exception_WhenADifferentUserRequestsUpdateThanWhoCreatedIt() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		final ApiRecipe recipe = new ApiRecipe("id", "name", "content", false);

		try {
			when(translator.recipeIdFor("id")).thenReturn(recipeId);
			when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("userId")));

			endpoint.putRecipe(recipe, "id", "otherUserId");
			fail("expected exception");
		} catch(WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("Only the original creator of a recipe may update it.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void rateRecipe_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final RecipeId recipeId = new RecipeId("id");
		final Recipe ratedRecipe = new Recipe(recipeId, "name", "content", new UserId("user"));
		final ApiRecipe apiRecipe = new ApiRecipe("id", "name", "content", false);

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.rateRecipe(recipeId, userId, 5)).thenReturn(ratedRecipe);
		when(translator.toApi(ratedRecipe, userId)).thenReturn(apiRecipe);

		final ApiRecipe results = endpoint.rateRecipe("id", new ApiRatingRequest(5), "user");
		assertThat(results).isEqualTo(apiRecipe);
	}

	@Test
	public void rateRecipe_Throws401_WhenNullUserIdIsSpecified() throws Exception {
		try {
			endpoint.rateRecipe("id", new ApiRatingRequest(5), null);
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void rateRecipe_Throws400_WhenValueIsOutOfRange() throws Exception {
		when(translator.userIdFor("user")).thenReturn(new UserId("user"));
		try {
			endpoint.rateRecipe("id", new ApiRatingRequest(6), "user");
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getResponse().getStatus()).isEqualTo(400);
		}
	}

	@Test
	public void rateRecipe_ThrowsNotFound_WhenRecipeIsMissing() throws Exception {
		final UserId userId = new UserId("user");
		final RecipeId recipeId = new RecipeId("id");
		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		final NoRecipeExistsForIdException repositoryException = new NoRecipeExistsForIdException(recipeId);
		when(repository.rateRecipe(recipeId, userId, 5)).thenThrow(repositoryException);

		try {
			endpoint.rateRecipe("id", new ApiRatingRequest(5), "user");
			fail("expecting exception");
		} catch (final NotFoundException e) {
			assertThat(e.getCause()).isEqualTo(repositoryException);
		}
	}

	@Test
	public void getTagSuggestions_delegatesToRepository() throws Exception {
		final List<String> suggestions = List.of("Vegetarian", "Quick");
		when(repository.suggestTagsForCategory("Main Dish", 24)).thenReturn(suggestions);

		final List<String> result = endpoint.getTagSuggestions("Main Dish");

		assertThat(result).isEqualTo(suggestions);
	}

	@Test
	public void getTagSuggestions_blankCategory_returnsEmptyWithoutHittingRepository() throws Exception {
		assertThat(endpoint.getTagSuggestions("")).isEmpty();
		assertThat(endpoint.getTagSuggestions(null)).isEmpty();
		verifyNoInteractions(repository);
	}
}
