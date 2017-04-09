package com.poorknight.recipe;

import com.poorknight.recipe.Recipe.RecipeId;
import com.poorknight.recipe.Recipe.UserId;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RecipeTranslatorTest {

	private static final String ID_1 = RandomStringUtils.random(12);
	private static final String NAME_1 = RandomStringUtils.random(12);
	private static final String CONTENT_1 = RandomStringUtils.random(12);
	private static final String USER_ID_1 = RandomStringUtils.random(10);
	private static final String IMAGE_URL = RandomStringUtils.random(30);
	private static final String IMAGE_ID = RandomStringUtils.random(25);
	private static final RecipeImage RECIPE_IMAGE = new RecipeImage(IMAGE_ID, IMAGE_URL);

	private static final String ID_2 = RandomStringUtils.random(12);
	private static final String NAME_2 = RandomStringUtils.random(12);
	private static final String CONTENT_2 = RandomStringUtils.random(12);
	private static final String USER_ID_2 = RandomStringUtils.random(10);

	private final RecipeTranslator translator = new RecipeTranslator();

	@Test
	public void toApi_TranslatesSingleRecipe() throws Exception {
		final Recipe recipe = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1, new UserId(USER_ID_1), RECIPE_IMAGE);
		final ApiRecipe translatedRecipe = translator.toApi(recipe, new UserId(USER_ID_1));

		assertThat(translatedRecipe.getRecipeId()).isEqualTo(ID_1);
		assertThat(translatedRecipe.getRecipeName()).isEqualTo(NAME_1);
		assertThat(translatedRecipe.getRecipeContent()).isEqualTo(CONTENT_1);
		assertThat(translatedRecipe.getEditable()).isEqualTo(true);
		assertThat(translatedRecipe.getImage()).isEqualTo(RECIPE_IMAGE);
	}

	@Test
	public void toApi_WithUserIdThatIsNotARecipesOwner_ReturnsFalseForEditable() throws Exception {
		final Recipe recipe = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1, new UserId(USER_ID_1));
		final ApiRecipe translatedRecipe = translator.toApi(recipe, new UserId(USER_ID_2));

		assertThat(translatedRecipe.getEditable()).isEqualTo(false);
	}

	@Test
	public void toApi_WhereRecipeHasNullOwner_ReturnsFalseForEditable() throws Exception {
		final Recipe recipe = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1, null);
		final ApiRecipe translatedRecipe = translator.toApi(recipe, new UserId(USER_ID_2));

		assertThat(translatedRecipe.getEditable()).isEqualTo(false);
	}

	@Test
	public void toApi_WithUserIdThatIsNull_ReturnsFalseForEditable() throws Exception {
		final Recipe recipe = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1, new UserId(USER_ID_1));
		final ApiRecipe translatedRecipe = translator.toApi(recipe, null);

		assertThat(translatedRecipe.getEditable()).isEqualTo(false);
	}

	@Test
	public void oApi_WithBothIdsNull_ReturnsFalseForEditable() throws Exception {
		final Recipe recipe = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1, null);
		final ApiRecipe translatedRecipe = translator.toApi(recipe, null);

		assertThat(translatedRecipe.getEditable()).isEqualTo(false);
	}

	@Test
	public void toApi_TranslatesMutipleRecipes() throws Exception {
		final Recipe recipe1 = new Recipe(new RecipeId(ID_1), NAME_1, CONTENT_1, new UserId(USER_ID_1));
		final Recipe recipe2 = new Recipe(new RecipeId(ID_2), NAME_2, CONTENT_2, new UserId(USER_ID_2));
		final List<ApiRecipe> translatedRecipes = translator.toApi(Arrays.asList(recipe1, recipe2), new UserId(USER_ID_1));

		assertThat(translatedRecipes.size()).isEqualTo(2);

		final ApiRecipe firstRecipe = translatedRecipes.get(0);
		assertThat(firstRecipe.getRecipeId()).isEqualTo(ID_1);
		assertThat(firstRecipe.getRecipeName()).isEqualTo(NAME_1);
		assertThat(firstRecipe.getRecipeContent()).isEqualTo(CONTENT_1);
		assertThat(firstRecipe.getEditable()).isEqualTo(true);

		final ApiRecipe secondRecipe = translatedRecipes.get(1);
		assertThat(secondRecipe.getRecipeId()).isEqualTo(ID_2);
		assertThat(secondRecipe.getRecipeName()).isEqualTo(NAME_2);
		assertThat(secondRecipe.getRecipeContent()).isEqualTo(CONTENT_2);
		assertThat(secondRecipe.getEditable()).isEqualTo(false);
	}

	@Test
	public void toDomain_WithId_TranslatesCorrectly() throws Exception {
		final ApiRecipe apiRecipe = new ApiRecipe(ID_1, NAME_1, CONTENT_1, false, RECIPE_IMAGE);
		final Recipe translatedRecipe = translator.toDomain(apiRecipe, new UserId(USER_ID_1));

		assertThat(translatedRecipe.getId().getValue()).isEqualTo(ID_1);
		assertThat(translatedRecipe.getName()).isEqualTo(NAME_1);
		assertThat(translatedRecipe.getContent()).isEqualTo(CONTENT_1);
		assertThat(translatedRecipe.getOwningUserId()).isEqualTo(new UserId(USER_ID_1));
		assertThat(translatedRecipe.getImage()).isEqualTo(RECIPE_IMAGE);
	}

	@Test
	public void toDomain_WithNoId_TranslatesCorrectly() throws Exception {
		final ApiRecipe apiRecipe = new ApiRecipe(null, NAME_1, CONTENT_1, true, RECIPE_IMAGE);
		final Recipe translatedRecipe = translator.toDomain(apiRecipe, new UserId(USER_ID_1));

		assertThat(translatedRecipe.getId()).isNull();
		assertThat(translatedRecipe.getName()).isEqualTo(NAME_1);
		assertThat(translatedRecipe.getContent()).isEqualTo(CONTENT_1);
		assertThat(translatedRecipe.getOwningUserId()).isEqualTo(new UserId(USER_ID_1));
		assertThat(translatedRecipe.getImage()).isEqualTo(RECIPE_IMAGE);
	}

	@Test
	public void recipeIdFor_AnyString_TranslatesCorrectly() throws Exception {
		assertThat(translator.recipeIdFor(ID_1)).isEqualTo(new RecipeId(ID_1));
	}

	@Test
	public void recipeIdFor_Null_ReturnsNull() {
		assertThat(translator.recipeIdFor(null)).isNull();
	}

	@Test
	public void userIdFor_AnyString_TranslatesCorrectly() throws Exception {
		assertThat(translator.userIdFor(USER_ID_1)).isEqualTo(new UserId(USER_ID_1));
	}

	@Test
	public void userIdFor_Null_ReturnsNull() {
		assertThat(translator.userIdFor(null)).isNull();
	}

	@Test
	public void userIdFor_EmptyString_ReturnsNull() {
		assertThat(translator.userIdFor("")).isNull();
	}
}
