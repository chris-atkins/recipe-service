package com.poorknight.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.poorknight.api.entities.ApiUser;
import com.poorknight.domain.User;
import com.poorknight.domain.identities.UserId;
import com.poorknight.persistence.UserRepository;
import com.poorknight.transform.api.domain.UserTranslator;

@RunWith(MockitoJUnitRunner.class)
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
	public void postUser_ReturnsUserFromRepositorySaveMethod() throws Exception {
		final ApiUser apiUser = Mockito.mock(ApiUser.class);
		final User userToSave = Mockito.mock(User.class);
		when(userTranslator.toDomain(apiUser)).thenReturn(userToSave);

		final User savedUser = Mockito.mock(User.class);
		when(userRepository.saveUser(userToSave)).thenReturn(savedUser);

		final ApiUser translatedUser = Mockito.mock(ApiUser.class);
		when(userTranslator.toApi(savedUser)).thenReturn(translatedUser);

		final ApiUser userFromEndpoint = userEndpoint.postUser(apiUser);
		assertThat(userFromEndpoint).isEqualTo(translatedUser);
	}
}
