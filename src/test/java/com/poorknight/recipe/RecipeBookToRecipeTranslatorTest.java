package com.poorknight.recipe;

import com.poorknight.recipebook.ApiRecipeId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RecipeBookToRecipeTranslatorTest {

	private RecipeBookToRecipeTranslator translator = new RecipeBookToRecipeTranslator();

	@Test
	public void translateIds_TranslatesCorrectly() throws Exception {
		List<ApiRecipeId> apiRecipeIds = Arrays.asList(new ApiRecipeId("id1"), new ApiRecipeId("id2"));
		final List<Recipe.RecipeId> recipeIds = translator.translateIds(apiRecipeIds);

		assertThat(recipeIds.size()).isEqualTo(2);
		assertThat(recipeIds.get(0).getValue()).isEqualTo("id1");
		assertThat(recipeIds.get(1).getValue()).isEqualTo("id2");
	}

	@Test
	public void translateIds_ReturnsAnEmptyListIfPassedNull() throws Exception {
		final List<Recipe.RecipeId> recipeIds = translator.translateIds(null);
		assertThat(recipeIds.size()).isEqualTo(0);
	}

	@Test
	public void translateIds_DoesNotIncludeNullOrNullValueOrEmptyValueIds() throws Exception {
		List<ApiRecipeId> apiRecipeIds = Arrays.asList(null, new ApiRecipeId("id1"), new ApiRecipeId(""), new ApiRecipeId(null));
		final List<Recipe.RecipeId> recipeIds = translator.translateIds(apiRecipeIds);

		assertThat(recipeIds.size()).isEqualTo(1);
		assertThat(recipeIds.get(0).getValue()).isEqualTo("id1");
	}
}