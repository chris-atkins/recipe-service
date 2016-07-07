package com.poorknight.api;

import com.poorknight.recipebook.ApiRecipeId;
import com.poorknight.recipebook.RecipeBook;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import com.poorknight.recipebook.RecipeBookRepository;
import com.poorknight.recipebook.RecipeBookTranslator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipeBookEndpointTest {

	@InjectMocks
	private RecipeBookEndpoint recipeBookEndpoint;

	@Mock
	private RecipeBookRepository recipeBookRepository;

	@Mock
	private RecipeBookTranslator recipeBookTranslator;

	@Test
	public void post_DelegatesToItsCollaborators() throws Exception {
		String userIdString = randomIdString();
		ApiRecipeId apiRecipeId = new ApiRecipeId(randomIdString());

		RecipeId recipeId = new RecipeId(randomIdString());
		UserId userId = new UserId(randomIdString());
		RecipeId savedRecipeId = new RecipeId(randomIdString());
		ApiRecipeId translatedResults = new ApiRecipeId(randomIdString());

		when(recipeBookTranslator.toDomain(apiRecipeId)).thenReturn(recipeId);
		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);
		when(recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId)).thenReturn(savedRecipeId);
		when(recipeBookTranslator.recipeIdFor(savedRecipeId)).thenReturn(translatedResults);

		final ApiRecipeId result = recipeBookEndpoint.postIdToRecipeBook(userIdString, apiRecipeId);
		assertThat(result).isEqualTo(translatedResults);
	}

	@Test
	public void get_DelegatesToItsCollaborators() throws Exception {
		String userIdString = randomIdString();
		UserId userId = new UserId(randomIdString());
		RecipeBook recipeBook = new RecipeBook(userId, Collections.emptyList());
		List<ApiRecipeId> translatedResults = new ArrayList<>();

		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);
		when(recipeBookRepository.getRecipeBook(userId)).thenReturn(recipeBook);
		when(recipeBookTranslator.toApi(recipeBook)).thenReturn(translatedResults);

		final List<ApiRecipeId> result = recipeBookEndpoint.getRecipeBook(userIdString);
		assertThat(result).isEqualTo(translatedResults);
	}

	private String randomIdString() {
		return RandomStringUtils.random(15);
	}
}