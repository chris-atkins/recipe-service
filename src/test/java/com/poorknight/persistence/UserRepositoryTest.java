package com.poorknight.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.mongodb.MongoClient;
import com.poorknight.domain.User;
import com.poorknight.mongo.setup.MongoSetupHelper;

@RunWith(JUnit4.class)
public class UserRepositoryTest {

	private static MongoClient mongo;
	private UserRepository userRepository;

	@BeforeClass
	public static void setupMongo() throws Exception {
		mongo = MongoSetupHelper.startMongoInstance();
	}

	@AfterClass
	public static void teardown() {
		MongoSetupHelper.cleanupMongo();
	}

	@Before
	public void setup() {
		userRepository = new UserRepository(mongo);
	}

	@After
	public void tearDown() {
		MongoSetupHelper.deleteAllUsers();
	}

	@Test
	public void savesAndGetsBasicUser() throws Exception {
		final User userToSave = new User("name", "email");

		final User savedUser = userRepository.saveUser(userToSave);
		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getName()).isEqualTo("name");
		assertThat(savedUser.getEmail()).isEqualTo("email");

		final User foundUser = userRepository.findUserById(savedUser.getId());
		assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
		assertThat(foundUser.getName()).isEqualTo("name");
		assertThat(foundUser.getEmail()).isEqualTo("email");
	}

	@Test
	public void canFindUserByEmail() throws Exception {
		final User userToSave = new User("thename", "theBest@EmailEver.com");
		final User savedUser = userRepository.saveUser(userToSave);

		final User foundUser = userRepository.findUserByEmail("thebest@emailever.com");
		assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
		assertThat(foundUser.getEmail()).isEqualTo("theBest@EmailEver.com");
		assertThat(foundUser.getName()).isEqualTo("thename");
	}
}
