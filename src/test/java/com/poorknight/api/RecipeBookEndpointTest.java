package com.poorknight.api;

import com.poorknight.recipebook.ApiRecipeBook.ApiRecipeId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class RecipeBookEndpointTest {

	RecipeBookEndpoint recipeBookEndpoint = new RecipeBookEndpoint();

	@Test
	public void postReturnsTheObjectItWasCalledWith() throws Exception {
		ApiRecipeId recipeId = new ApiRecipeId();
		ApiRecipeId result = recipeBookEndpoint.postIdToRecipeBook("userId", recipeId);
		assertThat(result).isEqualTo(recipeId);
	}
}