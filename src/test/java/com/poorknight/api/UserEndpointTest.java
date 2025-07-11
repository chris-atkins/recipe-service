package com.poorknight.api;

import com.poorknight.user.ApiUser;
import com.poorknight.user.User;
import com.poorknight.user.User.UserId;
import com.poorknight.user.UserRepository;
import com.poorknight.user.UserTranslator;
import com.poorknight.user.save.NonUniqueEmailException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserEndpointTest {

	@InjectMocks
	private UserEndpoint userEndpoint;

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserTranslator userTranslator;

	@Test
	public void getUserById_ReturnsUserFromRepository() throws Exception {
		final String userId = RandomStringUtils.random(20);

		final UserId userIdentity = Mockito.mock(UserId.class);
		when(userTranslator.userIdFor(userId)).thenReturn(userIdentity);

		final User userFromRepository = Mockito.mock(User.class);
		when(userRepository.findUserById(userIdentity)).thenReturn(userFromRepository);

		final ApiUser translatedUser = Mockito.mock(ApiUser.class);
		when(userTranslator.toApi(userFromRepository)).thenReturn(translatedUser);

		final ApiUser userFromEndpoint = userEndpoint.getUser(userId);
		assertThat(userFromEndpoint).isEqualTo(translatedUser);
	}

	@Test
	public void getUserById_ReturnsNotFound_WhenNoUserExists() throws Exception {
		final String userId = RandomStringUtils.random(20);

		final UserId userIdentity = Mockito.mock(UserId.class);
		when(userTranslator.userIdFor(userId)).thenReturn(userIdentity);

		when(userRepository.findUserById(userIdentity)).thenReturn(null);

		try {
			userEndpoint.getUser(userId);
			fail("expecting exception");
		} catch (NotFoundException e) {
			assertThat(e.getMessage()).isEqualTo("User not found: " + userId);
		}
	}

	@Test
	public void getUserByEmail_ReturnsUserFromRepository() throws Exception {
		final String userEmail = RandomStringUtils.random(20);
		final User userFromRepository = Mockito.mock(User.class);
		when(userRepository.findUserByEmail(userEmail)).thenReturn(userFromRepository);

		final ApiUser translatedUser = Mockito.mock(ApiUser.class);
		when(userTranslator.toApi(userFromRepository)).thenReturn(translatedUser);

		final ApiUser userFromEndpoint = userEndpoint.getUserByEmail(userEmail);
		assertThat(userFromEndpoint).isEqualTo(translatedUser);
	}

	@Test
	public void getUserByEmail_ReturnsNotFound_WhenNoUserExists() throws Exception {
		final String userEmail = RandomStringUtils.random(20);
		when(userRepository.findUserByEmail(userEmail)).thenReturn(null);

		try {
			userEndpoint.getUserByEmail(userEmail);
			fail("expecting exception");
		} catch(NotFoundException e) {
			assertThat(e.getMessage()).isEqualTo("User not found: " + userEmail);
		}
	}

	@Test
	public void postUser_ReturnsUserFromRepositorySaveMethod() throws Exception {
		final ApiUser apiUser = Mockito.mock(ApiUser.class);
		final User userToSave = Mockito.mock(User.class);
		when(userTranslator.toDomain(apiUser)).thenReturn(userToSave);

		final User savedUser = Mockito.mock(User.class);
		when(userRepository.saveNewUser(userToSave)).thenReturn(savedUser);

		final ApiUser translatedUser = Mockito.mock(ApiUser.class);
		when(userTranslator.toApi(savedUser)).thenReturn(translatedUser);

		final ApiUser userFromEndpoint = userEndpoint.postUser(apiUser);
		assertThat(userFromEndpoint).isEqualTo(translatedUser);
	}

	@Test
	public void postUser_ThrowsForbiddenException_WhenRepositoryThrowsNonUniqueEmailException() throws Exception {
		final ApiUser apiUser = Mockito.mock(ApiUser.class);
		final User userToSave = Mockito.mock(User.class);
		when(userTranslator.toDomain(apiUser)).thenReturn(userToSave);
		when(userRepository.saveNewUser(userToSave)).thenThrow(new NonUniqueEmailException("email@e.com"));

		try {
			userEndpoint.postUser(apiUser);
			fail("Expecting exception");

		} catch (final ForbiddenException e) {
			assertThat(e.getMessage()).isEqualTo("Attempting to save a new user with an email that already exists: email@e.com");
		}
	}
}
