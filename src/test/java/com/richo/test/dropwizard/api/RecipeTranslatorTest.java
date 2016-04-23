package com.richo.test.dropwizard.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.poorknight.api.ApiRecipe;
import com.poorknight.domain.Recipe;
import com.poorknight.domain.identities.RecipeId;

@RunWith(JUnit4.class)
public class RecipeTranslatorTest {

	private static final String ID_1 = RandomStringUtils.random(12);
	private static final String NAME_1 = RandomStringUtils.random(12);
	private static final String CONTENT_1 = RandomStringUtils.random(12);

	private static final String ID_2 = RandomStringUtils.random(12);
	private static final String NAME_2 = RandomStringUtils.random(12);
	private static final String CONTENT_2 = RandomStringUtils.random(12);

	private final RecipeTranslator translator = new RecipeTranslator();

	@Test
	public void translatesSingleRecipe_FromDomainToApi() throws Exception {
		final Recipe recipe = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1);
		final ApiRecipe translatedRecipe = translator.toApi(recipe);

		assertThat(translatedRecipe.getId()).isEqualTo(ID_1);
		assertThat(translatedRecipe.getName()).isEqualTo(NAME_1);
		assertThat(translatedRecipe.getContent()).isEqualTo(CONTENT_1);
	}

	@Test
	public void translatesMutipleRecipes_FromDomainToApi() throws Exception {
		final Recipe recipe1 = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1);
		final Recipe recipe2 = new Recipe(new RecipeId(ID_2), NAME_2, CONTENT_2);
		final List<ApiRecipe> translatedRecipes = translator.toApi(Arrays.asList(recipe1, recipe2));

		assertThat(translatedRecipes.size()).isEqualTo(2);

		final ApiRecipe firstRecipe = translatedRecipes.get(0);
		assertThat(firstRecipe.getId()).isEqualTo(ID_1);
		assertThat(firstRecipe.getName()).isEqualTo(NAME_1);
		assertThat(firstRecipe.getContent()).isEqualTo(CONTENT_1);

		final ApiRecipe secondRecipe = translatedRecipes.get(1);
		assertThat(secondRecipe.getId()).isEqualTo(ID_2);
		assertThat(secondRecipe.getName()).isEqualTo(NAME_2);
		assertThat(secondRecipe.getContent()).isEqualTo(CONTENT_2);
	}

	@Test
	public void toDomain_WithId_TranslatesCorrectly() throws Exception {
		final ApiRecipe apiRecipe = new ApiRecipe(ID_1, NAME_1, CONTENT_1);
		final Recipe translatedRecipe = translator.toDomain(apiRecipe);

		assertThat(translatedRecipe.getId().getValue()).isEqualTo(ID_1);
		assertThat(translatedRecipe.getName()).isEqualTo(NAME_1);
		assertThat(translatedRecipe.getContent()).isEqualTo(CONTENT_1);
	}

	@Test
	public void toDomain_WithNoId_TranslatesCorrectly() throws Exception {
		final ApiRecipe apiRecipe = new ApiRecipe(null, NAME_1, CONTENT_1);
		final Recipe translatedRecipe = translator.toDomain(apiRecipe);

		assertThat(translatedRecipe.getId()).isNull();
		assertThat(translatedRecipe.getName()).isEqualTo(NAME_1);
		assertThat(translatedRecipe.getContent()).isEqualTo(CONTENT_1);
	}
}
