package com.poorknight.user;

import com.poorknight.mongo.setup.PostgresTestHelper;
import com.poorknight.recipe.PostgresConnectionInfo;
import com.poorknight.user.save.NonUniqueEmailException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PostgresUserRepositoryTest {

	private UserRepository userRepository;

	@BeforeAll
	public static void setupMongo() throws Exception {
		PostgresTestHelper.startPostgresAndMigrateTables();
	}

	@AfterAll
	public static void teardown() {
		PostgresTestHelper.stopPostgres();
	}

	@BeforeEach
	public void setup() {
		PostgresConnectionInfo connectionInfo = PostgresTestHelper.buildCoonnectionInfo();
		userRepository = new PostgresUserRepository(connectionInfo);
	}

	@AfterEach
	public void tearDown() {
		PostgresTestHelper.deleteAllUsers();
	}

	@Test
	public void savesAndGetsBasicUser() throws Exception {
		final User userToSave = new User("name", "email");

		final User savedUser = userRepository.saveNewUser(userToSave);
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
		final User savedUser = userRepository.saveNewUser(userToSave);

		final User foundUser = userRepository.findUserByEmail("theBest@EmailEver.com");
		assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
		assertThat(foundUser.getEmail()).isEqualTo("theBest@EmailEver.com");
		assertThat(foundUser.getName()).isEqualTo("thename");
	}

	@Test
	public void findUserByEmail_IsCaseSensitive() throws Exception {
		final User userToSave = new User("thename", "theBest@EmailEver.com");
		userRepository.saveNewUser(userToSave);

		final User foundUser = userRepository.findUserByEmail("thebest@emailever.com");
		assertThat(foundUser).isNull();
	}

	@Test
	public void saveNewUser_WithRepeatEmail_ThrowsException() throws Exception {
		final User firstUser = new User("anyName", "repeatEmail");
		final User secondUser = new User("differentName", "repeatEmail");

		userRepository.saveNewUser(firstUser);

		try {
			userRepository.saveNewUser(secondUser);
			fail("expecting exception");

		} catch (final NonUniqueEmailException e) {
			assertThat(e.getMessage()).isEqualTo("Attempting to save a new user with an email that already exists: repeatEmail");
		}
	}
}
