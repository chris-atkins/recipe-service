package com.poorknight.recipebook;

import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RecipeBookTranslatorTest {

	private final RecipeBookTranslator recipeBookTranslator = new RecipeBookTranslator();

	@Test
	public void apiRecipeIdFor_TranslatesCorrectly() throws Exception {
		final String recipeIdString = RandomStringUtils.random(10);
		final RecipeId recipeId = new RecipeId(recipeIdString);

		final ApiRecipeId apiRecipeId = recipeBookTranslator.apiRecipeIdFor(recipeId);
		assertThat(apiRecipeId.getRecipeId()).isEqualTo(recipeIdString);
	}

	@Test
	public void apiRecipeIdFor_WithNullRecipeId_ReturnsNull() throws Exception {
		final ApiRecipeId apiRecipeId = recipeBookTranslator.apiRecipeIdFor(null);
		assertThat(apiRecipeId).isNull();
	}

	@Test
	public void apiRecipeIdFor_WithNullRecipeIdValue_ReturnsNull() throws Exception {
		final ApiRecipeId apiRecipeId = recipeBookTranslator.apiRecipeIdFor(new RecipeId(null));
		assertThat(apiRecipeId).isNull();
	}

	@Test
	public void apiRecipeIdFor_WithEmptyRecipeIdValue_ReturnsNull() throws Exception {
		final ApiRecipeId apiRecipeId = recipeBookTranslator.apiRecipeIdFor(new RecipeId(""));
		assertThat(apiRecipeId).isNull();
	}

	@Test
	public void toDomain_TranslatesCorrectly() throws Exception {
		final String recipeIdString = RandomStringUtils.random(10);
		final ApiRecipeId apiRecipeId = new ApiRecipeId(recipeIdString);

		final RecipeId recipeId = recipeBookTranslator.toDomain(apiRecipeId);
		assertThat(recipeId.getValue()).isEqualTo(recipeIdString);
	}

	@Test
	public void toDomain_WithNullApiRecipeId_ReturnsNull() throws Exception {
		final RecipeId recipeId = recipeBookTranslator.toDomain(null);
		assertThat(recipeId).isNull();
	}

	@Test
	public void toDomain_WithNullApiRecipeIdValue_ReturnsNull() throws Exception {
		final RecipeId recipeId = recipeBookTranslator.toDomain(new ApiRecipeId(null));
		assertThat(recipeId).isNull();
	}

	@Test
	public void toDomain_WithEmptyApiRecipeIdValue_ReturnsNull() throws Exception {
		final RecipeId recipeId = recipeBookTranslator.toDomain(new ApiRecipeId(""));
		assertThat(recipeId).isNull();
	}

	@Test
	public void userIdFor_TranslatesCorrectly() throws Exception {
		final String userIdString = RandomStringUtils.random(10);
		final UserId userId = recipeBookTranslator.userIdFor(userIdString);
		assertThat(userId.getValue()).isEqualTo(userIdString);
	}

	@Test
	public void recipeIdFor_TranslatesCorrectly() throws Exception {
		final String recipeIdString = RandomStringUtils.random(10);
		final RecipeId recipeId = recipeBookTranslator.recipeIdFor(recipeIdString);
		assertThat(recipeId.getValue()).isEqualTo(recipeIdString);
	}

	@Test
	public void userIdFor_WithNullParameter_ReturnsNull() throws Exception {
		final UserId userId = recipeBookTranslator.userIdFor(null);
		assertThat(userId).isNull();
	}

	@Test
	public void userIdFor_WithEmptyParameter_ReturnsNull() throws Exception {
		final UserId userId = recipeBookTranslator.userIdFor("");
		assertThat(userId).isNull();
	}

	@Test
	public void toApi_TranslatesCorrectly() throws Exception {
		final String recipeId1String = RandomStringUtils.random(10);
		final String recipeId2String = RandomStringUtils.random(10);

		final RecipeId recipeId1 = new RecipeId(recipeId1String);
		final RecipeId recipeId2 = new RecipeId(recipeId2String);
		final RecipeBook recipeBook = new RecipeBook(new UserId(RandomStringUtils.random(15)), Arrays.asList(recipeId1, recipeId2));

		final List<ApiRecipeId> apiRecipeIds = recipeBookTranslator.toApi(recipeBook);
		assertThat(apiRecipeIds.size()).isEqualTo(2);
		assertThat(apiRecipeIds.get(0).getRecipeId()).isEqualTo(recipeId1String);
		assertThat(apiRecipeIds.get(1).getRecipeId()).isEqualTo(recipeId2String);
	}

	@Test
	public void toApi_ReturnsEmptyList_ForNullParameter() throws Exception {
		final List<ApiRecipeId> apiRecipeIds = recipeBookTranslator.toApi(null);
		assertThat(apiRecipeIds.size()).isEqualTo(0);
	}
}