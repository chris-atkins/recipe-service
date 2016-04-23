package com.richo.test.dropwizard.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.poorknight.api.ApiRecipe;
import com.poorknight.domain.Recipe;
import com.poorknight.domain.identities.RecipeId;
import com.poorknight.persistence.RecipeRepository;

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

	@Test
	public void getRecipes_DelegatesToItsCollaborators() throws Exception {
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
		final Recipe translatedRecipe = new Recipe("name", "content");
		final Recipe savedRecipe = new Recipe(new RecipeId("id"), "name", "content");
		final ApiRecipe translatedSavedRecipe = new ApiRecipe("id", "name", "content");

		when(translator.toDomain(recipe)).thenReturn(translatedRecipe);
		when(repository.saveNewRecipe(translatedRecipe)).thenReturn(savedRecipe);
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
}
