package com.poorknight.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.NotFoundException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.poorknight.recipe.ApiRecipe;
import com.poorknight.recipe.Recipe;
import com.poorknight.recipe.RecipeId;
import com.poorknight.recipe.RecipeRepository;
import com.poorknight.recipe.RecipeTranslator;
import com.poorknight.recipe.save.TextToHtmlTranformer;
import com.poorknight.recipe.search.RecipeSearchStringParser;
import com.poorknight.recipe.search.SearchTag;

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
		final List<Recipe> recipesFromRepository = Collections.singletonList(new Recipe(null, null, null));
		final List<ApiRecipe> translatedRecipes = Collections.singletonList(new ApiRecipe());

		when(repository.findAllRecipes()).thenReturn(recipesFromRepository);
		when(translator.toApi(recipesFromRepository)).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes(null);
		assertThat(results).isSameAs(translatedRecipes);
	}

	@Test
	public void getRecipes_WithSearchString_DelegatesToItsCollaborators() throws Exception {
		final List<Recipe> recipesFromRepository = Collections.singletonList(new Recipe(null, null, null));
		final List<ApiRecipe> translatedRecipes = Collections.singletonList(new ApiRecipe());
		final List<SearchTag> searchTags = Collections.singletonList(new SearchTag(""));

		when(recipeSearchStringParser.parseSearchString("search string")).thenReturn(searchTags);
		when(repository.searchRecipes(searchTags)).thenReturn(recipesFromRepository);
		when(translator.toApi(recipesFromRepository)).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes("search string");
		assertThat(results).isSameAs(translatedRecipes);
	}

	@Test
	public void postRecipe_DelegatesToItsCollaborators() throws Exception {
		final ApiRecipe recipe = new ApiRecipe("name", "content");
		final Recipe translatedRecipe = new Recipe("hi", "content");
		final Recipe expectedRecipeAfterHtmlTranslation = new Recipe("hi", "htmlified");
		final Recipe savedRecipe = new Recipe(new RecipeId("id"), "hi", "htmlified");
		final ApiRecipe translatedSavedRecipe = new ApiRecipe("id", "hi", "htmlified");

		when(translator.toDomain(recipe)).thenReturn(translatedRecipe);
		when(htmlTransformer.translate("content")).thenReturn("htmlified");
		when(repository.saveNewRecipe(expectedRecipeAfterHtmlTranslation)).thenReturn(savedRecipe);
		when(translator.toApi(savedRecipe)).thenReturn(translatedSavedRecipe);

		final ApiRecipe results = endpoint.postRecipe(recipe);
		assertThat(results).isEqualTo(translatedSavedRecipe);
	}

	@Test
	public void getRecipe_DelegatesToItsCollaborators() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		final Recipe domainRecipe = new Recipe(recipeId, "name", "content");
		final ApiRecipe apiRecipe = new ApiRecipe("id", "name", "content");

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(domainRecipe);
		when(translator.toApi(domainRecipe)).thenReturn(apiRecipe);

		final ApiRecipe results = endpoint.getRecipe("id");
		assertThat(results).isEqualTo(apiRecipe);
	}

	@Test(expected = NotFoundException.class)
	public void getRecipe_ThrowsNotFoundException_IfNoRecipeExists() throws Exception {
		final RecipeId recipeId = new RecipeId("id");

		when(translator.recipeIdFor("id")).thenReturn(recipeId);
		when(repository.findRecipeById(recipeId)).thenReturn(null);

		endpoint.getRecipe("id");
	}

	@Test
	public void deleteRecipe_DelegatesToItsCollaborators() throws Exception {
		final RecipeId recipeId = new RecipeId("id");
		when(translator.recipeIdFor("id")).thenReturn(recipeId);

		endpoint.deleteRecipe("id");

		verify(repository).deleteRecipe(recipeId);
	}

	@Test
	public void testName() throws Exception {
		assertTrue(false);
	}
}
