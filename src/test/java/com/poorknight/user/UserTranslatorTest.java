package com.poorknight.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.poorknight.user.User;
import com.poorknight.user.UserId;
import com.poorknight.user.UserTranslator;

@RunWith(JUnit4.class)
public class UserTranslatorTest {

	private final UserTranslator userTranslator = new UserTranslator();

	@Test
	public void toApi_TranslatesCorrectly() throws Exception {
		final String userId = RandomStringUtils.random(20);
		final String name = RandomStringUtils.random(20);
		final String email = RandomStringUtils.random(20);
		final User domainUser = new User(new UserId(userId), name, email);

		final ApiUser apiUser = userTranslator.toApi(domainUser);

		assertThat(apiUser.getUserId()).isEqualTo(userId);
		assertThat(apiUser.getUserName()).isEqualTo(name);
		assertThat(apiUser.getUserEmail()).isEqualTo(email);
	}

	@Test
	public void toDomain_WithNullId_TranslatesCorrectly() throws Exception {
		final String name = RandomStringUtils.random(20);
		final String email = RandomStringUtils.random(20);
		final ApiUser apiUser = new ApiUser(null, name, email);

		final User domainUser = userTranslator.toDomain(apiUser);

		assertThat(domainUser.getId()).isNull();
		assertThat(domainUser.getName()).isEqualTo(name);
		assertThat(domainUser.getEmail()).isEqualTo(email);
	}

	@Test
	public void toDomain_WithId_TranslatesCorrectly() throws Exception {
		final String id = RandomStringUtils.random(20);
		final String name = RandomStringUtils.random(20);
		final String email = RandomStringUtils.random(20);
		final ApiUser apiUser = new ApiUser(id, name, email);

		final User domainUser = userTranslator.toDomain(apiUser);

		assertThat(domainUser.getId().getValue()).isEqualTo(id);
		assertThat(domainUser.getName()).isEqualTo(name);
		assertThat(domainUser.getEmail()).isEqualTo(email);
	}

	@Test
	public void userIdFor_TranslatesCorrectly() throws Exception {
		final String idValue = RandomStringUtils.random(20);
		final UserId userId = userTranslator.userIdFor(idValue);
		assertThat(userId.getValue()).isEqualTo(idValue);
	}

	@Test
	public void userIdFor_ReturnsNullForNullInput() throws Exception {
		final UserId userId = userTranslator.userIdFor(null);
		assertThat(userId).isNull();
	}
}
