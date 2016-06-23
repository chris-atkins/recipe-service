package com.poorknight.api;

import com.poorknight.recipe.ApiRecipe;
import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.Recipe.UserId;
import com.poorknight.recipe.RecipeRepository;
import com.poorknight.recipe.RecipeTranslator;
import com.poorknight.recipe.exception.NoRecipeExistsForIdException;
import com.poorknight.recipe.save.TextToHtmlTranformer;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.recipe.search.SearchTag;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
	private TextToHtmlTranformer htmlTransformer;

	@Test
	public void getRecipes_WithNoSearchString_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final List<Recipe> recipesFromRepository = Collections.singletonList(new Recipe(null, null, null));
		final List<ApiRecipe> translatedRecipes = Collections.singletonList(new ApiRecipe());

		when(translator.userIdFor("user")).thenReturn(userId);
		when(repository.findAllRecipes()).thenReturn(recipesFromRepository);
		when(translator.toApi(recipesFromRepository, userId)).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes(null, "user");
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

		final List<ApiRecipe> results = endpoint.getRecipes("search string", "user");
		assertThat(results).isSameAs(translatedRecipes);
	}

	@Test
	public void postRecipe_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final ApiRecipe recipe = new ApiRecipe("name", "content", false);
		final Recipe translatedRecipe = new Recipe("hi", "content", new UserId("user"));
		final Recipe expectedRecipeAfterHtmlTranslation = new Recipe("hi", "htmlified", new UserId("user"));
		final Recipe savedRecipe = new Recipe(new RecipeId("id"), "hi", "htmlified", new UserId("user"));
		final ApiRecipe translatedSavedRecipe = new ApiRecipe("id", "hi", "htmlified", false);

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.toDomain(recipe, new UserId("user"))).thenReturn(translatedRecipe);
		when(htmlTransformer.translate("content")).thenReturn("htmlified");
		when(repository.saveNewRecipe(expectedRecipeAfterHtmlTranslation)).thenReturn(savedRecipe);
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

	@Test(expected = NotFoundException.class)
	public void getRecipe_ThrowsNotFoundException_IfNoRecipeExists() throws Exception {
		final RecipeId recipeId = new RecipeId("id");

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(null);

		endpoint.getRecipe("id", "user");
	}

	@Test
	public void deleteRecipe_DelegatesToItsCollaborators() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		when(translator.recipeIdFor("id")).thenReturn(recipeId);

		endpoint.deleteRecipe("id", "user");

		verify(repository).deleteRecipe(recipeId);
	}

	@Test
	public void putRecipe_DelegatesToItsCollaborators() throws Exception {
		final UserId userId = new UserId("user");
		final RecipeId recipeId = new RecipeId("id");
		final ApiRecipe recipe = new ApiRecipe("id", "name", "content", false);
		final Recipe translatedRecipe = new Recipe(new RecipeId("id"), "hi", "content", new UserId("user"));
		final Recipe expectedRecipeAfterHtmlTranslation = new Recipe(new RecipeId("id"), "hi", "htmlified", new UserId("user"));
		final Recipe updatedRecipe = new Recipe(new RecipeId("id"), "hi", "htmlified", new UserId("user"));
		final ApiRecipe translatedUpdatedRecipe = new ApiRecipe("id", "hi", "htmlified", false);

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("user")));

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.toDomain(recipe, new UserId("user"))).thenReturn(translatedRecipe);
		when(htmlTransformer.translate("content")).thenReturn("htmlified");
		when(repository.updateRecipe(expectedRecipeAfterHtmlTranslation)).thenReturn(updatedRecipe);
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
		final Recipe expectedRecipeAfterHtmlTranslation = new Recipe(new RecipeId("id"), "hi", "htmlified", new UserId("user"));

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(new Recipe("", "", new UserId("user")));

		when(translator.userIdFor("user")).thenReturn(userId);
		when(translator.toDomain(recipe, new UserId("user"))).thenReturn(translatedRecipe);
		when(htmlTransformer.translate("content")).thenReturn("htmlified");
		final NoRecipeExistsForIdException repositoryException = new NoRecipeExistsForIdException(recipeId);
		when(repository.updateRecipe(expectedRecipeAfterHtmlTranslation)).thenThrow(repositoryException);

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
}
