package com.poorknight.api;

import com.poorknight.recipebook.*;
import com.poorknight.recipebook.RecipeBook.RecipeId;
import com.poorknight.recipebook.RecipeBook.UserId;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
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
		final String userIdString = randomIdString();
		final ApiRecipeId apiRecipeId = new ApiRecipeId(randomIdString());

		final RecipeId recipeId = new RecipeId(randomIdString());
		final UserId userId = new UserId(randomIdString());
		final RecipeId savedRecipeId = new RecipeId(randomIdString());
		final ApiRecipeId translatedResults = new ApiRecipeId(randomIdString());

		when(recipeBookTranslator.toDomain(apiRecipeId)).thenReturn(recipeId);
		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);
		when(recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId)).thenReturn(savedRecipeId);
		when(recipeBookTranslator.apiRecipeIdFor(savedRecipeId)).thenReturn(translatedResults);

		final ApiRecipeId result = recipeBookEndpoint.postIdToRecipeBook(userIdString, apiRecipeId, userIdString);
		assertThat(result).isEqualTo(translatedResults);
	}

	@Test
	public void post_WithBadId_Throws400Exception() throws Exception {
		final String userIdString = randomIdString();
		final ApiRecipeId apiRecipeId = new ApiRecipeId(randomIdString());

		final RecipeId recipeId = new RecipeId(randomIdString());
		final UserId userId = new UserId(randomIdString());

		when(recipeBookTranslator.toDomain(apiRecipeId)).thenReturn(recipeId);
		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);
		when(recipeBookRepository.addRecipeIdToRecipeBook(userId, recipeId)).thenThrow(new InvalidIdException("hi"));

		try {
			recipeBookEndpoint.postIdToRecipeBook(userIdString, apiRecipeId, userIdString);
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("The passed ID is not valid: hi");
			assertThat(e.getResponse().getStatus()).isEqualTo(400);
		}
	}

	@Test
	public void post_ToARecipeBookForADifferentUser_Throws401Exception() throws Exception {
		final String userIdString = "userIdString";

		try {
			recipeBookEndpoint.postIdToRecipeBook("aDifferentUser", null, userIdString);
			fail("expecting exception");

		} catch (final WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid user. Only the owner of a recipe book may alter it.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void get_DelegatesToItsCollaborators() throws Exception {
		final String userIdString = randomIdString();
		final UserId userId = new UserId(randomIdString());
		final RecipeBook recipeBook = new RecipeBook(userId, Collections.emptyList());
		final List<ApiRecipeId> translatedResults = new ArrayList<>();

		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);
		when(recipeBookRepository.getRecipeBook(userId)).thenReturn(recipeBook);
		when(recipeBookTranslator.toApi(recipeBook)).thenReturn(translatedResults);

		final List<ApiRecipeId> result = recipeBookEndpoint.getRecipeBook(userIdString);
		assertThat(result).isEqualTo(translatedResults);
	}

	@Test
	public void get_WithABadUserId_ReturnsAnEmptyList() throws Exception {
		final UserId userId = new UserId("invalid");

		when(recipeBookTranslator.userIdFor("invalid")).thenReturn(userId);
		when(recipeBookRepository.getRecipeBook(userId)).thenThrow(new InvalidIdException("hi"));

		final List<ApiRecipeId> results = recipeBookEndpoint.getRecipeBook("invalid");
		assertThat(results.size()).isEqualTo(0);
	}

	@Test
	public void deleteRecipeFromBook_DelegatesToItsCollaborators() throws Exception {
		final String userIdString = randomIdString();
		final String recipeIdString = randomIdString();

		final RecipeId recipeId = new RecipeId(randomIdString());
		final UserId userId = new UserId(randomIdString());

		when(recipeBookTranslator.recipeIdFor(recipeIdString)).thenReturn(recipeId);
		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);

		recipeBookEndpoint.deleteRecipeFromBook(userIdString, recipeIdString, userIdString);

		Mockito.verify(recipeBookRepository).deleteRecipeFromRecipeBook(userId, recipeId);
	}

	@Test
	public void deleteRecipeFromBook_WhenRecipeIsNotInBook_Throws404Error() throws Exception {
		final String userIdString = randomIdString();
		final String recipeIdString = randomIdString();

		final RecipeId recipeId = new RecipeId(randomIdString());
		final UserId userId = new UserId(randomIdString());
		final RecipeNotInBookException exceptionToThrow = new RecipeNotInBookException(recipeId);

		when(recipeBookTranslator.recipeIdFor(recipeIdString)).thenReturn(recipeId);
		when(recipeBookTranslator.userIdFor(userIdString)).thenReturn(userId);
		Mockito.doThrow(exceptionToThrow).when(recipeBookRepository).deleteRecipeFromRecipeBook(userId, recipeId);

		try {
			recipeBookEndpoint.deleteRecipeFromBook(userIdString, recipeIdString, userIdString);
			fail("expecting exception");
		} catch(final NotFoundException e) {
			assertThat(e.getCause()).isEqualTo(exceptionToThrow);
		}
	}

	@Test
	public void deleteRecipeFromBook_WhenNoRequestingUserExists_Throws401Error() throws Exception {
		final String userIdString = randomIdString();
		final String recipeIdString = randomIdString();

		try {
			recipeBookEndpoint.deleteRecipeFromBook(userIdString, recipeIdString, null);
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid user. Only the owner of a recipe book may alter it.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	@Test
	public void deleteRecipeFromBook_WhenRequestingUserIsDifferentThanOwningUser_Throws401Error() throws Exception {
		final String userIdString = randomIdString();
		final String requestingUserIdString = "differentUser";
		final String recipeIdString = randomIdString();

		try {
			recipeBookEndpoint.deleteRecipeFromBook(userIdString, recipeIdString, requestingUserIdString);
			fail("expecting exception");
		} catch (final WebApplicationException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid user. Only the owner of a recipe book may alter it.");
			assertThat(e.getResponse().getStatus()).isEqualTo(401);
		}
	}

	private String randomIdString() {
		return RandomStringUtils.random(15);
	}
}