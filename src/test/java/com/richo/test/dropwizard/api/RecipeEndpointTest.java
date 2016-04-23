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
		when(translator.translate(recipesFromRepository)).thenReturn(translatedRecipes);

		final List<ApiRecipe> results = endpoint.getRecipes("search string");
		assertThat(results).isSameAs(translatedRecipes);
	}
}
